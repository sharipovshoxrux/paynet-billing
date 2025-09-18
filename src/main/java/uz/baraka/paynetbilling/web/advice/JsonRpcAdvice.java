package uz.baraka.paynetbilling.web.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class JsonRpcAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonRpcModels.Response> badParams(IllegalArgumentException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(null, -32602, e.getMessage()));
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<JsonRpcModels.Response> notFound(NoSuchElementException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(null, 404, e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcModels.Response> serverErr(Exception e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(null, -32000, "Server error"));
    }
}
