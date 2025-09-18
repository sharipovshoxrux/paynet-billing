package uz.baraka.paynetbilling.web.rpc;

import java.util.Map;

public interface RpcHandler {
    String method();
    JsonRpcModels.Response handle(Object id, Map<String,Object> params);
}
