package uz.baraka.paynetbilling.exception;

public class TransactionAlreadyCancelledException extends RuntimeException {
    public TransactionAlreadyCancelledException(String msg) { super(msg); }
}
