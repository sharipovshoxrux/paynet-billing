package uz.baraka.paynetbilling.web.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.baraka.paynetbilling.exception.InvalidAmountException;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcRequestIdHolder;

import java.util.NoSuchElementException;

@RestControllerAdvice
@RequiredArgsConstructor
public class JsonRpcAdvice {
    private final RpcRequestIdHolder idHolder;

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<JsonRpcModels.Response> invalidAmount(InvalidAmountException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 413, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonRpcModels.Response> badParams(IllegalArgumentException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), -32602, e.getMessage()));
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<JsonRpcModels.Response> notFound(NoSuchElementException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 404, e.getMessage()));
    }
    @ExceptionHandler(NoSuchApplicationException.class)
    public ResponseEntity<JsonRpcModels.Response> applicationNotFound(NoSuchApplicationException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 302, e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcModels.Response> serverErr(Exception e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), -32000, "Server error"));
    }
}
