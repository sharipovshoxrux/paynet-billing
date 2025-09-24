package uz.baraka.paynetbilling.exception;

public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String msg) { super(msg); }
}
