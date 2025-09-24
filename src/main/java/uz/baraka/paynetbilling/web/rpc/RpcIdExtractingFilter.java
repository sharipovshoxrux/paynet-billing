package uz.baraka.paynetbilling.web.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.baraka.paynetbilling.config.CachedBodyHttpServletRequest;

import java.io.IOException;

@Component
public class RpcIdExtractingFilter extends OncePerRequestFilter {

    private final RpcRequestIdHolder holder;
    private final ObjectMapper om = new ObjectMapper();

    public RpcIdExtractingFilter(RpcRequestIdHolder holder) {
        this.holder = holder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/v1/billing/rpc")
                || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        byte[] body = request.getInputStream().readAllBytes();

        try {
            if (body.length > 0) {
                JsonNode root = om.readTree(body);
                JsonNode id = root.get("id");
                if (id != null && !id.isNull()) {
                    if (id.isNumber()) holder.set(id.numberValue());
                    else if (id.isTextual()) holder.set(id.textValue());
                    else holder.set(id.toString());
                }
            }
        } catch (Exception ignored) {
        }

        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, body);
        try {
            chain.doFilter(wrapped, response);
        } finally {
            holder.clear();
        }
    }
}
