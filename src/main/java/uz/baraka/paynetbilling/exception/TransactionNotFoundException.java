package uz.baraka.paynetbilling.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String msg) { super(msg); }
}
