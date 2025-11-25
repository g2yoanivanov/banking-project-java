package fmi.yoan.bank.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Loan {
    private int id;
    private double amount;
    private double remainingAmount;

    private LocalDate startDate;
    private LocalDate dueDate;

    public Loan() {

    }
}
