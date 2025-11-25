package fmi.yoan.bank.server;

import java.util.TreeSet;

public class Account {
    private int id;
    private String name;
    private String egn;
    private String IBAN;
    private String PIN;

    private double balance;
    private TreeSet<Loan> loans;
}
