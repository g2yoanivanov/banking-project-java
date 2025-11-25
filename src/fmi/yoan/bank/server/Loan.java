package fmi.yoan.bank.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Loan implements Comparable<Loan> {
    private String id;
    private double amount;
    private double remainingAmount;

    private LocalDate startDate;
    private LocalDate dueDate;

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

        this.startDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusMonths(months);
    }

    @Override
    public int compareTo(Loan other) {
        int dateCompare = this.startDate.compareTo(other.startDate);

        if(dateCompare != 0) {
            return dateCompare;
        }

        return this.id.compareTo(other.id);
    }
}
