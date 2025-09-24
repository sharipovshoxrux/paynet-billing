package uz.baraka.paynetbilling.web.rpc;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Map;

public final class JsonRpcModels {

    public record ApplicationInfo(String applicationId, String name, BigDecimal amount, boolean paid) {}
    public record CheckByTxResult(long providerTrnId, int transactionState) {}

    public record Request(String jsonrpc, String method, Object id, Map<String, Object> params) {}

    public record Response(String jsonrpc, Object id,
                           @JsonInclude(JsonInclude.Include.NON_NULL) Object result,
                           @JsonInclude(JsonInclude.Include.NON_NULL) Error error) {
        public static Response ok(Object id, Result result) {
            return new Response("2.0", id, result, null);
        }
        public static Response okFlat(Object id, FlatResult result) {
            return new Response("2.0", id, result, null);
        }
        public static Response err(Object id, int code, String message) {
            return new Response("2.0", id, null, new Error(code, message));
        }
    }

    public record Result(
            @JsonInclude(JsonInclude.Include.NON_NULL) String status,
            @JsonInclude(JsonInclude.Include.NON_NULL) String timestamp,
            Map<String, Object> fields) {}

    public record Error(int code, String message) {}

    public record FlatResult(
            @JsonInclude(JsonInclude.Include.NON_NULL) String timestamp,
            @JsonIgnore Map<String, Object> values
    ) {
        @JsonAnyGetter
        public Map<String,Object> any() { return values; }
    }

    public record Fields(String application_id) {}

    public record GetInfoParams(Fields fields) {}

    public record PerformParams(BigDecimal amount, Long transactionId, Integer serviceId, Fields fields) {}

    public record CheckParams(Long transactionId, Integer serviceId, String timestamp) {}

    public record CancelParams(Long transactionId, Integer serviceId, String timestamp) {}
}
