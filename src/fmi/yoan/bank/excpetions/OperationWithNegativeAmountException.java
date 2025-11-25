package fmi.yoan.bank.excpetions;

public class OperationWithNegativeAmountException extends RuntimeException {
    public OperationWithNegativeAmountException(String message) {
        super(message);
    }
}
