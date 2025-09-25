package uz.baraka.paynetbilling.web.rpc.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
class GetStatementHandler implements RpcHandler {
    private final BillingService billing;

    @Override public String method(){ return "GetStatement"; }

    @Override
    public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        Integer serviceId = null;
        Object sid = params.get("serviceId");
        if (sid != null) serviceId = Integer.valueOf(String.valueOf(sid));
        var from = parseTs(String.valueOf(params.get("dateFrom")));
        var to   = parseTs(String.valueOf(params.get("dateTo")));

        var rows = billing.statementForReconciliation(from, to, serviceId);

        var statements = new ArrayList<Map<String,Object>>(rows.size());
        for (var r : rows) {
            var m = new LinkedHashMap<String,Object>(4);
            m.put("amount",        r.amount().longValueExact());
            m.put("providerTrnId", r.providerTrnId());
            m.put("transactionId", r.transactionId() != null ? r.transactionId() : 0L);
            m.put("timestamp",     r.timestamp());
            statements.add(m);
        }

        return JsonRpcModels.Response.okFlat(
                id,
                new JsonRpcModels.FlatResult(null, Map.of("statements", statements))
        );
    }

    private static LocalDateTime parseTs(String v) {
        var A = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var B = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        try { return LocalDateTime.parse(v, A); } catch(Exception ignored){}
        try { return LocalDateTime.parse(v, B); } catch(Exception ignored){}
        throw new IllegalArgumentException("Invalid timestamp");
    }
}
