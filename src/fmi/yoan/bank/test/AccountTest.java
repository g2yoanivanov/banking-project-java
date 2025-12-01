package fmi.yoan.bank.test;

import fmi.yoan.bank.exceptions.InsufficientFundsException;
import fmi.yoan.bank.exceptions.OperationWithNegativeAmountException;
import fmi.yoan.bank.server.Account;
import fmi.yoan.bank.server.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountTest {
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("Yoan Ivanov", "1010101010",
                "BG53G2YB00000000100000", "1234", 0.00);
    }

    @Test
    void testDepositIncrease() {
        account.deposit(500);
        assertEquals(500, account.getBalance(), "Balance must increase");
    }


    @Test
    void testWithdrawDecrease() {
        account.deposit(500);
        account.withdraw(100);
        assertEquals(400, account.getBalance(), "Balance must decrease");
    }

    @Test
    void testNegativeOperationsException() {
        assertThrows(OperationWithNegativeAmountException.class, () -> account.deposit(-100));
        assertThrows(OperationWithNegativeAmountException.class, () -> account.withdraw(-100));
    }

    @Test
    void testInsufficientFundsException() {
        assertThrows(InsufficientFundsException.class, () -> {
            account.withdraw(1000);
        });
    }

    @Test
    void testPayLoanInstallmentLogic() {
        Loan loan = new Loan(500, 12);
        account.addLoan(loan);
        account.deposit(500);
        String loanId = loan.getId();

        double paid = account.payLoanInstallment(loanId, 100);

        assertEquals(100, paid);
        assertEquals(400, loan.getRemainingAmount());
        assertEquals(400, account.getBalance(), "Balance must increase");
    }
}
