package uz.baraka.paynetbilling.web.rpc.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.web.JsonRpcController;
import uz.baraka.paynetbilling.web.rpc.JsonParamBinder;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcHandler;

import java.time.Clock;
import java.util.Map;

@Component
@RequiredArgsConstructor
class CheckTransactionHandler implements RpcHandler {
    private final JsonParamBinder binder;
    private final BillingService billing;
    private final Clock clock;

    @Override public String method(){ return "CheckTransaction"; }

    @Override public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        var p = binder.bind(params, JsonRpcModels.CheckParams.class);
        boolean paid = billing.isAlreadyPaid(p.fields().application_id());
        var fields = Map.<String, Object>of("applicationId", p.fields().application_id(), "status", paid ? "PAID" : "NOT_PAID");
        return JsonRpcModels.Response.ok(id, new JsonRpcModels.Result(JsonRpcController.now(clock), fields));
    }
}
