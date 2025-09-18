package uz.baraka.paynetbilling.web.rpc;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RpcRegistry {
    private final Map<String, RpcHandler> handlers;
    public RpcRegistry(List<RpcHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(RpcHandler::method, h -> h));
    }
    public RpcHandler get(String method) {
        var h = handlers.get(method);
        if (h == null) throw new IllegalArgumentException("Method not found");
        return h;
    }
}
