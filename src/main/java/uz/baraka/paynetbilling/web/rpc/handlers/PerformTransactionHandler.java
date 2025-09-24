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
class PerformTransactionHandler implements RpcHandler {
    private final JsonParamBinder binder;
    private final BillingService billing;
    private final Clock clock;

    @Override public String method(){ return "PerformTransaction"; }

    @Override public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        var p = binder.bind(params, JsonRpcModels.PerformParams.class);

        if (billing.isAlreadyPaid(p.fields().application_id()))
            return JsonRpcModels.Response.err(id, 201, "Транзакция уже существует");

        var info = billing.perform(p.fields().application_id(), p.amount());
        var fields = Map.<String, Object>of("name", info.name(), "amount", info.amount(), "paid", info.paid());
        return JsonRpcModels.Response.ok(id, new JsonRpcModels.Result(null, JsonRpcController.now(clock), fields));
    }
}
