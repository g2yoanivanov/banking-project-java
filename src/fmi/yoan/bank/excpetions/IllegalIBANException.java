package fmi.yoan.bank.excpetions;

public class IllegalIBANException extends RuntimeException {
    public IllegalIBANException(String message) {
        super(message);
    }
}
