package fmi.yoan.bank.excpetions;

public class IllegalLoanPeriodException extends RuntimeException {
    public IllegalLoanPeriodException(String message) {
        super(message);
    }
}
