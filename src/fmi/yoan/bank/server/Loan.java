package fmi.yoan.bank.server;

import fmi.yoan.bank.exceptions.OperationWithNegativeAmountException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Loan implements Comparable<Loan> {
    private final String id;
    private final double amount;
    private double remainingAmount;

    private boolean isPaid;

    private final LocalDateTime startDate;
    private final LocalDateTime dueDate;

    public Loan(double amount, int months) {
        if(months < 0) {
            throw new IllegalArgumentException("Cannot request a loan for negative months");
        }

        if(months > 60) {
            throw new IllegalArgumentException("Cannot request a loan for more than 60 months");
        }

        if(amount <= 0) {
            throw new OperationWithNegativeAmountException("Cannot request a loan for negative or zero amount");
        }

        this.id =  UUID.randomUUID().toString();
        this.amount = amount;
        this.remainingAmount = amount;

        this.isPaid = false;

        this.startDate = LocalDateTime.now();
        this.dueDate = this.startDate.plusMonths(months);
    }

    public void pay(double amount) {
        if(amount < 0) {
            throw new OperationWithNegativeAmountException("Cannot pay a loan with negative amount");
        }

        this.remainingAmount -= amount;
    }

    public boolean isPaid() {
        if(remainingAmount < 0.0001) {
            this.isPaid = true;
        }

        return isPaid;
    }

    public boolean isDue() {
        return dueDate.isBefore(LocalDateTime.now());
    }

    public double applyPenaltyInterest() {
        double penalty = 0;
        double PENALTY_INTEREST = 0.01;

        penalty =  PENALTY_INTEREST * this.remainingAmount;
        this.remainingAmount += penalty;

        return penalty;
    }

    @Override
    public int compareTo(Loan other) {
        int dateCompare = this.startDate.compareTo(other.startDate);

        if(dateCompare != 0) {
            return dateCompare;
        }

        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return String.format(
                "[Loan] ID: %s%n" +
                "Given on: %s%n" +
                "Due date: %s%n" +
                "Remaining amount: %.2f (Starting amount: %.2f)%n",
                id, dtf.format(startDate), dtf.format(dueDate), remainingAmount, amount
        );
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public double getRemainingAmount() { return remainingAmount; }
}
