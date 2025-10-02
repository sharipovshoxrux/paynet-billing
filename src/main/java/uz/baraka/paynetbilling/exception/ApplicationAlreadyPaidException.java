package uz.baraka.paynetbilling.exception;

public class ApplicationAlreadyPaidException extends RuntimeException {
    public ApplicationAlreadyPaidException(String message) {
        super(message);
    }
}
