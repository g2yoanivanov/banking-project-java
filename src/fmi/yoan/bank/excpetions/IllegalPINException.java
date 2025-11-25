package fmi.yoan.bank.excpetions;

public class IllegalPINException extends RuntimeException {
    public IllegalPINException(String message) {
        super(message);
    }
}
