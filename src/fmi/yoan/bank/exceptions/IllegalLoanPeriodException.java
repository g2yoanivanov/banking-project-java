package fmi.yoan.bank.exceptions;

public class IllegalLoanPeriodException extends RuntimeException {
    public IllegalLoanPeriodException(String message) {
        super(message);
    }
}
