package fmi.yoan.bank.server;

import fmi.yoan.bank.exceptions.InsufficientFundsException;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Bank {
    private static final String COUNTRY_CODE = "BG";
    private static final double MAX_LOAN_AMOUNT = 50000.00;
    private static final int MAX_LOAN_COUNT = 5;
    private static final int MAX_LOAN_PERIOD = 60;

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

    public synchronized String getBankReserves() {
        return String.format("%.2f", this.reserves);
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

    public synchronized String changePin(String iban, String oldPin, String newPin) {
        if(oldPin == null || newPin == null) {
            throw new NullPointerException("PIN cannot be null");
        }

        if(iban == null) {
            throw new NullPointerException("IBAN cannot be null");
        }

        if(oldPin.equals(newPin)) {
            throw  new IllegalArgumentException("The new PIN cannot be same as the old one");
        }

        if(!accountsByIban.containsKey(iban)) {
            throw  new IllegalArgumentException("Account with this IBAN does not exist");
        }

        Account acc = accountsByIban.get(iban);

        if(!checkPin(acc.getPIN(), oldPin)) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        acc.setPin(newPin);

        return String.format("Successfully changed PIN!%n"
        + "New PIN: %s%n", newPin);
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
        try {
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

            acc.withdraw(amount);
            this.reserves -= amount;

            return String.format("Successfully withdrawn amount!%n"
                    + "New balance: %fBGN%n", acc.getBalance());
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String transfer(String senderIban, String receiverIban, String pin, double amount) {
        try {
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

            if(receiverIban.equals(senderIban)) {
                throw new IllegalArgumentException("Cannot transfer money to yourselfx");
            }

            if(!accountsByIban.containsKey(senderIban) ||  !accountsByIban.containsKey(receiverIban)) {
                throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
            }

            Account senderAcc = accountsByIban.get(senderIban);
            Account receiverAcc = accountsByIban.get(receiverIban);

            if(!checkPin(senderAcc.getPIN(), pin)) {
                throw new IllegalArgumentException("Invalid PIN");
            }

            senderAcc.withdraw(amount);
            receiverAcc.deposit(amount);

            return String.format("Successfully transferred %.2f to %s!%n", amount, senderAcc.getIBAN());
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String requestLoan(String iban, String pin, double amount, int months) {
        try {
            if(iban == null) {
                throw new NullPointerException("IBAN cannot be null");
            }

            if(pin == null) {
                throw new NullPointerException("PIN cannot be null");
            }

            if(months <= 0) {
                throw new IllegalArgumentException("Cannot request a loan for less or equal to 0 months");
            }

            if(months > MAX_LOAN_PERIOD) {
                throw new IllegalArgumentException(String.format("Cannot request a loan for %d months", MAX_LOAN_PERIOD));
            }

            if(amount <= 0) {
                throw new IllegalArgumentException("Cannot request an amount less or equal to 0");
            }

            if(amount > MAX_LOAN_AMOUNT) {
                throw new IllegalArgumentException("Cannot request an amount greater than max loan amount");
            }

            if(amount > this.reserves) {
                throw new InsufficientFundsException("Not enough funds in the bank to give out the loan");
            }

            if(!accountsByIban.containsKey(iban)) {
                throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
            }

            Account acc = accountsByIban.get(iban);

            if(!checkPin(acc.getPIN(), pin)) {
                throw new IllegalArgumentException("Invalid PIN");
            }

            if(acc.getLoans().size() >= MAX_LOAN_COUNT) {
                throw new IllegalArgumentException("Cannot request loans anymore! Maximum amount of loans reached!");
            }

            Loan loan = new Loan(amount, months);

            acc.addLoan(loan);
            acc.deposit(amount);
            this.reserves -= amount;

            return String.format("Successfully added loan to account with IBAN: %s%n!" +
                    "%s%n", acc.getIBAN(), loan.toString());
        } catch(Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String payLoanInstallment(String iban, String pin, String loanId, double amount) {
        try {
            if(iban == null) {
                throw new NullPointerException("IBAN cannot be null");
            }

            if(pin == null) {
                throw new NullPointerException("PIN cannot be null");
            }

            if(loanId == null) {
                throw new NullPointerException("Loan ID cannot be null");
            }

            if(amount <= 0) {
                throw new IllegalArgumentException("Cannot pay an amount less or equal to 0");
            }

            if(!accountsByIban.containsKey(iban)) {
                throw new IllegalArgumentException("Cannot find an account with an existing IBAN");
            }

            Account acc = accountsByIban.get(iban);

            if(!checkPin(acc.getPIN(), pin)) {
                throw new IllegalArgumentException("Invalid PIN");
            }

            double paidAmount = acc.payLoanInstallment(loanId, amount);

            this.reserves += paidAmount;

            return String.format("Successfully paid installment! Paid: %.2f%n", paidAmount);
        } catch (Exception e) {
            return String.format("Error: %s%n", e.getMessage());
        }
    }

    public synchronized String getLoans() {
        return "";
    }


}
