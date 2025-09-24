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
class CancelTransactionHandler implements RpcHandler {
    private final JsonParamBinder binder;
    private final BillingService billing;
    private final Clock clock;

    @Override public String method() { return "CancelTransaction"; }

    @Override
    public JsonRpcModels.Response handle(Object id, Map<String,Object> params) {
        var p = binder.bind(params, JsonRpcModels.CancelParams.class);

        long providerTrnId = billing.cancelByTransactionId(p.transactionId());

        var flatValues = Map.<String,Object>of(
                "providerTrnId", providerTrnId,
                "transactionState", 2
        );

        return JsonRpcModels.Response.okFlat(
                id,
                new JsonRpcModels.FlatResult(JsonRpcController.now(clock), flatValues)
        );
    }
}
