package uz.baraka.paynetbilling.web.rpc.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
class GetStatementHandler implements RpcHandler {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final BillingService billing;

    @Override public String method(){ return "GetStatement"; }

    @Override public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        var from = parseTs(String.valueOf(params.get("dateFrom")));
        var to   = parseTs(String.valueOf(params.get("dateTo")));
        var rows = billing.statementForReconciliation(from, to);

        var list = new ArrayList<Map<String,Object>>(rows.size());
        for (var r : rows) {
            list.add(Map.of("applicationId", r.applicationId(), "name", r.name(), "amount", r.amount(), "timestamp", r.timestamp()));
        }
        return JsonRpcModels.Response.ok(id, new JsonRpcModels.Result(null, Map.of("statements", list)));
    }

    private static LocalDateTime parseTs(String v) {
        DateTimeFormatter A = TS, B = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        try { return LocalDateTime.parse(v, A); } catch(Exception ignored){}
        try { return LocalDateTime.parse(v, B); } catch(Exception ignored){}
        throw new IllegalArgumentException("Invalid timestamp");
    }
}
