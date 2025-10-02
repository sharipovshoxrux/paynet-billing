package uz.baraka.paynetbilling.exception;

public class ApplicationAlreadyProcessedException extends RuntimeException {
    public ApplicationAlreadyProcessedException(String message) {
        super(message);
    }
}
