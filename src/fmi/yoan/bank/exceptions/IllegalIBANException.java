package fmi.yoan.bank.exceptions;

public class IllegalIBANException extends RuntimeException {
    public IllegalIBANException(String message) {
        super(message);
    }
}
