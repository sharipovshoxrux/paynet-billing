package uz.baraka.paynetbilling.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcRegistry;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class JsonRpcController {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("GMT+5"));
    private final RpcRegistry registry;

    @PostMapping(value = "/rpc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcModels.Response rpc(@RequestBody JsonRpcModels.Request req) {
        return registry.get(req.method()).handle(req.id(), req.params());
    }

    public static String now(Clock clock) {
        return TS.format(Instant.now(clock));
    }
}
