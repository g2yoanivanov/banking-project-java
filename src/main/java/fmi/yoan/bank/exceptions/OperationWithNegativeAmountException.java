package fmi.yoan.bank.exceptions;

public class OperationWithNegativeAmountException extends RuntimeException {
    public OperationWithNegativeAmountException(String message) {
        super(message);
    }
}
