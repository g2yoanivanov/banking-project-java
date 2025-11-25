package fmi.yoan.bank.server;

import fmi.yoan.bank.excpetions.*;

import java.util.TreeSet;
import java.util.UUID;

public class Account {
    private final String id;
    private String name;
    private final String egn;
    private final String IBAN;
    private final String PIN;

    private double balance;
    private TreeSet<Loan> loans;

    private Loan findLoanById(String loanId) {
        for (Loan loan : loans) {
            if (loan.getId().equals(loanId)) {
                return loan;
            }
        }

        return null;
    }

    public Account(String name, String egn, String IBAN, String PIN, double balance) {
        if (name == null) {
            throw new NullPointerException("Account name cannot be null");
        }

        if (egn == null || egn.length() != 10) {
            throw new IllegalEGNException("Invalid EGN. Must be 10 digit number");
        }

        if(balance < 0) {
            throw new InsufficientFundsException("Balance cannot be negative");
        }

        if(IBAN == null || IBAN.length() != 22) {
            throw new IllegalIBANException("Invalid IBAN. Must be 22 digit number");
        }

        if(PIN == null || PIN.length() != 4) {
            throw new IllegalPINException("Invalid PIN. Must be 4 digit number");
        }

        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.egn = egn;
        this.IBAN = IBAN;
        this.PIN = PIN;
        this.balance = balance;
        this.loans = new TreeSet<>();
    }

    public synchronized void deposit(double amount) {
        if(amount < 0) {
            throw new OperationWithNegativeAmountException("Cannot deposit negative amount");
        }

        this.balance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount < 0) {
            throw new OperationWithNegativeAmountException("Cannot withdraw negative amount");
        }

        if (amount > balance) {
            throw new InsufficientFundsException("Cannot withdraw amount greater than balance");
        }

        this.balance -= amount;
    }

    public synchronized void addLoan(Loan loan) {
        this.loans.add(loan);
    }

    public synchronized double payLoanInstallment(String loanId, double amount) {
        if(amount < 0) {
            throw new OperationWithNegativeAmountException("Cannot pay negative amount");
        }

        if(amount > balance) {
            throw new InsufficientFundsException("Cannot pay amount greater than balance");
        }

        Loan loan = findLoanById(loanId);

        if(loan == null) {
            throw new LoanNotFoundException("Loan not found");
        }

        double remainingAmount = loan.getRemainingAmount();
        double paymentAmount = Math.min(amount, remainingAmount);

        if(this.balance < paymentAmount) {
            throw new InsufficientFundsException("Cannot pay amount greater than balance");
        }

        this.balance -= paymentAmount;
        loan.pay(paymentAmount);

        if(loan.isPaid()) {
            this.loans.remove(loan);
            System.out.printf("Loan %s has been paid", loan.getId());
        }

        return paymentAmount;
    }

    @Override
    public String toString() {
        return String.format(
                "[Account] ID: %s%n" +
                "Name: %s%n",
                id, name
        );
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getEgn() { return egn; }

    public String getIBAN() { return IBAN; }

    public String getPIN() { return PIN; }

    public synchronized double getBalance() { return balance; }

    public TreeSet<Loan> getLoans() { return loans; }
}
