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

    @Override
    public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        var p = binder.bind(params, JsonRpcModels.CheckParams.class);

        billing.requireTxAndValidateService(p.transactionId(), p.serviceId());

        var res = billing.checkByTransactionId(p.transactionId());

        var values = Map.<String,Object>of(
                "transactionState", res.transactionState(),
                "providerTrnId",    res.providerTrnId()
        );

        return JsonRpcModels.Response.okFlat(
                id,
                new JsonRpcModels.FlatResult(JsonRpcController.now(clock), values)
        );
    }
}
