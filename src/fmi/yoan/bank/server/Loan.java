package fmi.yoan.bank.server;

import fmi.yoan.bank.excpetions.OperationWithNegativeAmountException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Loan implements Comparable<Loan> {
    private String id;
    private final double amount;
    private double remainingAmount;

    private final LocalDateTime startDate;
    private final LocalDateTime dueDate;

    public Loan(double amount, int months) {
        if(months < 0) {
            throw new IllegalArgumentException("Cannot request a loan for negative months");
        }

        if(months > 60) {
            throw new IllegalArgumentException("Cannot request a loan for more than 60 months");
        }

        this.id =  UUID.randomUUID().toString();
        this.amount = amount;
        this.remainingAmount = amount;

        this.startDate = LocalDateTime.now();
        this.dueDate = LocalDateTime.now().plusMonths(months);
    }

    public void pay(double amount) {
        if(amount < 0) {
            throw new OperationWithNegativeAmountException("Cannot pay a loan with negative amount");
        }

        this.remainingAmount -= amount;
    }

    public Boolean isPaid() {
        return remainingAmount < 0.0001;
    }

    public Boolean isDue() {
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

    public String getId() { return id; }

    public double getAmount() { return amount; }

    public LocalDateTime getStartDate() { return startDate; }

    public LocalDateTime getDueDate() { return dueDate; }

    public double getRemainingAmount() { return remainingAmount; }
}
