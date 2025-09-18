package uz.baraka.paynetbilling.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class InternalKeyFilter extends OncePerRequestFilter {
    private final String headerName;
    private final String expectedValue;

    public InternalKeyFilter(String headerName, String expectedValue) {
        this.headerName = Objects.requireNonNullElse(headerName, "X-Internal-Key");
        this.expectedValue = expectedValue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (expectedValue == null || expectedValue.isBlank()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Internal key not configured");
            return;
        }

        String actual = request.getHeader(headerName);
        if (expectedValue.equals(actual)) {
            var auth = new UsernamePasswordAuthenticationToken("internal-service", null, List.of());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        }
    }
}
