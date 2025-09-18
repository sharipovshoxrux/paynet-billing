package uz.baraka.paynetbilling.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({PartnerAuthProps.class, InternalKeyProps.class})
public class SecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean AuthenticationEntryPoint basicEntryPoint() {
        var ep = new BasicAuthenticationEntryPoint();
        ep.setRealmName("paynet-billing");
        return ep;
    }

    @Bean
    UserDetailsService userDetailsService(PartnerAuthProps props) {
        InMemoryUserDetailsManager uds = new InMemoryUserDetailsManager();
        if (props.partners() != null) {
            for (PartnerAuthProps.Partner p : props.partners()) {
                if (!p.enabled()) continue;
                uds.createUser(
                        User.withUsername(p.username())
                                .password(p.passwordHash())
                                .build()
                );
            }
        }
        return uds;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    SecurityFilterChain billingChain(HttpSecurity http,
                                     AuthenticationManager authManager,
                                     AuthenticationEntryPoint entryPoint) throws Exception {
        http
                .securityMatcher("/api/v1/billing/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/billing/rpc").authenticated()
                        .anyRequest().denyAll()
                )
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint))
                .authenticationManager(authManager);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain applicationsChain(HttpSecurity http, InternalKeyProps props) throws Exception {
        String header = (props.headerName() == null || props.headerName().isBlank())
                ? "X-Internal-Key" : props.headerName();
        http.securityMatcher("/api/v1/applications/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new InternalKeyFilter(header, props.key()),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain everythingElse(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().denyAll()
                );
        return http.build();
    }
}
