package uz.baraka.paynetbilling.web.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyPaidException;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyProcessedException;
import uz.baraka.paynetbilling.exception.CannotCancelAfterOutcomeException;
import uz.baraka.paynetbilling.exception.InvalidAmountException;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.exception.ResourceAccessDeniedException;
import uz.baraka.paynetbilling.exception.ServiceNotFoundException;
import uz.baraka.paynetbilling.exception.TransactionAlreadyCancelledException;
import uz.baraka.paynetbilling.exception.TransactionAlreadyExistsException;
import uz.baraka.paynetbilling.exception.TransactionNotFoundException;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.RpcRequestIdHolder;

import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;

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
    @ExceptionHandler(TransactionAlreadyExistsException.class)
    public ResponseEntity<JsonRpcModels.Response> transactionAlreadyExists(TransactionAlreadyExistsException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 201, e.getMessage()));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<JsonRpcModels.Response> transactionNotFound(TransactionNotFoundException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 203, e.getMessage()));
    }

    @ExceptionHandler(TransactionAlreadyCancelledException.class)
    public ResponseEntity<JsonRpcModels.Response> transactionAlreadyCancelled(TransactionAlreadyCancelledException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 202, e.getMessage()));
    }

    @ExceptionHandler(CannotCancelAfterOutcomeException.class)
    public ResponseEntity<JsonRpcModels.Response> cannotCancelAfter(CancellationException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 77, e.getMessage()));
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<JsonRpcModels.Response> serviceNotFound(ServiceNotFoundException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 305, e.getMessage()));
    }

    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<JsonRpcModels.Response> resourceAccessDenied(ResourceAccessDeniedException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 403, e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<JsonRpcModels.Response> notFound(NoSuchElementException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 404, e.getMessage()));
    }
    @ExceptionHandler(NoSuchApplicationException.class)
    public ResponseEntity<JsonRpcModels.Response> applicationNotFound(NoSuchApplicationException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 302, e.getMessage()));
    }

    @ExceptionHandler(ApplicationAlreadyPaidException.class)
    public ResponseEntity<JsonRpcModels.Response> applicationAlreadyPaid(ApplicationAlreadyPaidException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 410, e.getMessage()));
    }

    @ExceptionHandler(ApplicationAlreadyProcessedException.class)
    public ResponseEntity<JsonRpcModels.Response> applicationAlreadyProcessed(ApplicationAlreadyProcessedException e) {
        return ResponseEntity.ok(JsonRpcModels.Response.err(idHolder.get(), 411, e.getMessage()));
    }
}
