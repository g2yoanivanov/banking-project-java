package fmi.yoan.bank.server;

import fmi.yoan.bank.excpetions.InsufficientFundsException;
import fmi.yoan.bank.excpetions.OperationWithNegativeAmountException;

import java.util.TreeSet;
import java.util.UUID;

public class Account {
    private String id;
    private String name;
    private String egn;
    private String IBAN;
    private String PIN;

    private double balance;
    private TreeSet<Loan> loans;

    public Account(String name, String egn, String IBAN, String PIN, double balance) {
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

    public synchronized void requestLoan() {

    }

    public synchronized void addLoan(Loan loan) {
        this.loans.add(loan);
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getEgn() { return egn; }

    public String getIBAN() { return IBAN; }

    public String getPIN() { return PIN; }

    public synchronized double getBalance() { return balance; }

    public TreeSet<Loan> getLoans() { return loans; }
}
