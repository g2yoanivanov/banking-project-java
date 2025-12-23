package fmi.yoan.bank.server;

import fmi.yoan.bank.exceptions.*;

import java.util.TreeSet;
import java.util.UUID;

public class Account {
    private final String id;
    private String name;
    private final String egn;
    private final String iban;
    private String pin;

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

    public Account(String name, String egn, String iban, String pin, double balance) {
        if (name == null) {
            throw new NullPointerException("Account name cannot be null");
        }

        if (egn == null || egn.length() != 10) {
            throw new IllegalArgumentException("Invalid EGN. Must be 10 digit number");
        }

        if(balance < 0) {
            throw new InsufficientFundsException("Balance cannot be negative");
        }

        if(iban == null || iban.length() != 22) {
            throw new IllegalArgumentException("Invalid IBAN. Must be 22 digit number");
        }

        if(pin == null || pin.length() != 4) {
            throw new IllegalArgumentException("Invalid PIN. Must be 4 digit number");
        }

        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.egn = egn;
        this.iban = iban;
        this.pin = pin;
        this.balance = balance;
        this.loans = new TreeSet<>();
    }

    public synchronized void deposit(double amount) {
        if(amount <= 0) {
            throw new OperationWithNegativeAmountException("Cannot deposit negative amount");
        }

        this.balance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount <= 0) {
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
        if(amount <= 0) {
            throw new OperationWithNegativeAmountException("Cannot pay negative or no amount");
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
        }

        return paymentAmount;
    }

    @Override
    public String toString() {
        return String.format(
                "[Account]%n"
                + "ID: %s%n"
                + "Name: %s%n"
                + "EGN: %s%n"
                + "IBAN: %s%n"
                + "PIN: %s%n"
                + "Balance: %.2f%n",
                id, name, egn, iban, pin, balance
        );
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getEgn() { return egn; }

    public String getIBAN() { return iban; }

    public synchronized String getPIN() { return pin; }

    public synchronized double getBalance() { return balance; }

    public synchronized TreeSet<Loan> getLoans() { return new TreeSet<>(this.loans); }

    public void setPin(String pin) {
        if(pin == null || pin.length() != 4) {
            throw new IllegalArgumentException("Invalid PIN. Must be 4 digit number");
        }

        this.pin = pin;
    }
}
