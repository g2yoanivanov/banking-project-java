package fmi.yoan.bank.test;

import fmi.yoan.bank.server.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankTest {
    private Bank bank;

    @BeforeEach
    public void setUp() {
        bank = new Bank("YoanBank", "G2YB", 1000000.00);
    }

    @Test
    void testRegisterCreatesAccount() {
        String response = bank.registerAccount("Yoan Ivanov", "1010101010");
        assertTrue(response.contains("Successfully"));
        assertTrue(response.contains("PIN"));
    }
}
