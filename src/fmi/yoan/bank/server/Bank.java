package fmi.yoan.bank.server;

import fmi.yoan.bank.exceptions.InsufficientFundsException;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Bank {
    private static String COUNTRY_CODE = "BG";
    private static double MAX_LOAN_AMOUNT = 50000.00;

    private final String name;
    private final String code;
    private Map<String, Account> accountsByIban;
    private Map<String, Account> accountsByEgn;
    private double reserves;
    private AtomicInteger ibanSequence;

    private String generateIban() {
        long uniqueNumber = ibanSequence.getAndIncrement();
        int safeDigits = 10 + (int)(Math.random() * 90);

        String formatedNumber = String.format("%014d", uniqueNumber);

        return String.format("%s%d%s%s", COUNTRY_CODE, safeDigits, this.code, formatedNumber);
    }

    private String generatePin() {
        Random random = new Random();
        int pin = random.nextInt(10000);

        return String.format("%04d", pin);
    }

    private boolean checkPin(String accPin, String enteredPin) {
        return enteredPin.equals(accPin);
    }

    Bank(String name, String code, double reserves) {
        if(name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if(code == null) {
            throw new NullPointerException("Code cannot be null");
        }

        if(reserves <= 0) {
            throw new IllegalArgumentException("Cannot create a bank with 0 or less reserves");
        }

        this.name = name;
        this.code = code;
        this.accountsByIban = new HashMap<>();
        this.accountsByEgn = new HashMap<>();
        this.reserves = reserves;
        this.ibanSequence = new AtomicInteger(100000);

    }

    public synchronized String registerAccount(String name, String egn) {
        if(name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if(egn == null) {
            throw new NullPointerException("Egn cannot be null");
        }

        if(accountsByEgn.containsKey(egn)) {
            throw  new IllegalArgumentException("Cannot register an account with an existing egn");
        }

        String iban = generateIban();
        String pin = generatePin();

        Account acc = new Account(name, egn, iban, pin, 0.0);
        accountsByIban.put(iban, acc);
        accountsByEgn.put(egn, acc);

        return String.format("Successfully registered an account!%n"
                + "%s%n"
                + "Welcome to %s%n",
                acc.toString(), this.name);
    }

    public synchronized String getAccountInfoByIban(String iban, String pin) {
        if(iban == null) {
            throw new NullPointerException("IBAN cannot be null");
        }

        if(pin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(!accountsByIban.containsKey(iban)) {
            throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
        }

        Account acc = accountsByIban.get(iban);

        if(!checkPin(acc.getPIN(), pin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        return acc.toString();
    }

    public synchronized String getAccountInfoByEgn(String egn, String pin) {
        if(egn == null) {
            throw new NullPointerException("EGN cannot be null");
        }

        if(pin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(!accountsByEgn.containsKey(egn)) {
            throw new IllegalArgumentException("Cannot find an account with an existing EGN");
        }

        Account acc = accountsByEgn.get(egn);

        if(!checkPin(acc.getPIN(), pin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        return acc.toString();
    }

    public synchronized String deposit(String iban, String pin, double amount) {
        if(iban == null) {
            throw new NullPointerException("IBAN cannot be null");
        }

        if(pin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(amount <= 0) {
            throw new IllegalArgumentException("Cannot deposit an amount less or equal to 0");
        }

        if(!accountsByIban.containsKey(iban)) {
            throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
        }

        Account acc = accountsByIban.get(iban);

        if(!checkPin(acc.getPIN(), pin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        try {
            acc.deposit(amount);
            this.reserves += amount;

            return String.format("Successfully deposited amount!%n"
                                + "New balance: %fBGN%n", acc.getBalance());
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String withdraw(String iban, String pin, double amount) {
        if(iban == null) {
            throw new NullPointerException("IBAN cannot be null");
        }

        if(pin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(amount <= 0) {
            throw new IllegalArgumentException("Cannot withdraw an amount less or equal to 0");
        }

        if(!accountsByIban.containsKey(iban)) {
            throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
        }

        Account acc = accountsByIban.get(iban);

        if(!checkPin(acc.getPIN(), pin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        try {
            acc.withdraw(amount);
            this.reserves -= amount;

            return String.format("Successfully withdrawn amount!%n"
                    + "New balance: %fBGN%n", acc.getBalance());
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String transfer(String senderIban, String receiverIban, String pin, double amount) {
        if(senderIban == null) {
            throw new NullPointerException("Sender IBAN cannot be null");
        }

        if(receiverIban == null) {
            throw new NullPointerException("Receiver IBAN cannot be null");
        }

        if(pin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(amount <= 0) {
            throw new IllegalArgumentException("Cannot transfer an amount less or equal to 0");
        }

        if(!accountsByIban.containsKey(senderIban) ||  !accountsByIban.containsKey(receiverIban)) {
            throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
        }

        Account senderAcc = accountsByIban.get(senderIban);
        Account receiverAcc = accountsByIban.get(receiverIban);

        if(!checkPin(senderAcc.getPIN(), pin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        try {
            senderAcc.withdraw(amount);
            receiverAcc.deposit(amount);

            return String.format("Successfully transferred amount!%n");
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }
}
