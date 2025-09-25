package uz.baraka.paynetbilling.exception;

public class CannotCancelAfterOutcomeException extends RuntimeException {
    public CannotCancelAfterOutcomeException(String msg) { super(msg); }
}
