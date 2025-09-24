package uz.baraka.paynetbilling.web.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Map;

public final class JsonRpcModels {
    public record Request(String jsonrpc, String method, Object id, Map<String, Object> params) {
    }

    public record Response(String jsonrpc, Object id, @JsonInclude(JsonInclude.Include.NON_NULL) Result result, Error error) {
        public static Response ok(Object id, Result result) {
            return new Response("2.0", id, result, null);
        }

        public static Response err(Object id, int code, String message) {
            return new Response("2.0", id, null, new Error(code, message));
        }
    }

    public record Result(@JsonInclude(JsonInclude.Include.NON_NULL) String status, @JsonInclude(JsonInclude.Include.NON_NULL) String timestamp, Map<String, Object> fields) {
    }

    public record Error(int code, String message) {
    }

    public record Fields(String application_id) {
        public Fields {
            if (application_id == null || application_id.isBlank())
                throw new IllegalArgumentException("fields.application_id is required");
        }
    }

    public record GetInfoParams(Fields fields) {
    }

    public record PerformParams(BigDecimal amount, Fields fields) {
    }

    public record CheckParams(Fields fields) {
    }

    public record CancelParams(Fields fields) {
    }
}
