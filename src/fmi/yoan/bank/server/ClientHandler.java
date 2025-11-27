package fmi.yoan.bank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Bank bank;
    private BufferedReader in;
    private PrintWriter out;

    private String currentIban = null;
    private String currentPin = null;

    ClientHandler(Socket clientSocket, Bank bank) {
        this.clientSocket = clientSocket;
        this.bank = bank;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("[System] Welcome to " + bank.getName());
            System.out.println("[System] Type HELP for list with commands");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] parts = inputLine.trim().split("\\s+");

                if (parts.length == 0 || parts[0].isEmpty()) {
                    continue;
                }

                String command = parts[0].toUpperCase();

                if ("EXIT".equals(command)) {
                    out.println("[System] Exiting...");
                    break;
                }

                String response = handleCommand(command, parts);
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("[System] The client stopped the connection");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String handleCommand(String command, String[] parts) {
        try {
            switch (command) {
                case "REGISTER":
                    if(parts.length != 3) {
                        return "[System] Usage: REGISTER <NAME> <EGN>";
                    }

                    String name = parts[1];
                    String egn = parts[2];

                    return bank.registerAccount(name, egn);
                case "LOGIN":
                    if(parts.length != 3) {
                        return "[System] Usage: LOGIN <IBAN> <PIN>";
                    }

                    String iban = parts[1];
                    String pin = parts[2];

                    String info = bank.getAccountInfoByIban(iban, pin);

                    this.currentIban = iban;
                    this.currentPin = pin;
                    return "[System] Account logged in successfully%n" + info;
                case "INFO":
                    if(parts.length != 1) {
                        return "[System] Usage: INFO";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    return bank.getAccountInfoByIban(this.currentIban, this.currentPin);
                case "DEPOSIT":
                    if(parts.length != 2) {
                        return "[System]  Usage: DEPOSIT <AMOUNT>";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    double depositAmount = Double.parseDouble(parts[1]);
                    return bank.deposit(this.currentIban, this.currentPin, depositAmount);
                case "WITHDRAW":
                    if(parts.length != 2) {
                        return "[System]  Usage: WITHDRAW <AMOUNT>";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    double withdrawAmount = Double.parseDouble(parts[1]);
                    return bank.withdraw(this.currentIban, this.currentPin, withdrawAmount);
                case "TRANSFER":
                    if(parts.length != 3) {
                        return "[System] Usage: TRANSFER <RECEIVER IBAN> <AMOUNT>";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    double transferAmount = Double.parseDouble(parts[2]);

                    return bank.transfer(this.currentIban, parts[1], this.currentPin, transferAmount);
                case "CHANGEPIN":
                    if(parts.length != 3) {
                        return "[System] Usage: CHANGEPIN <OLD PIN> <NEW PIN>]";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    if(!this.currentPin.equals(parts[1])) {
                        return "[System] Incorrect PIN entered%n";
                    }

                    String newPin = parts[2];

                    String result = bank.changePin(this.currentIban, this.currentPin, newPin);

                    if(result.startsWith("Successfully")) {
                        this.currentPin = newPin;
                    }

                    return result;
                case "REQLOAN":
                    if(parts.length != 3) {
                        return "[System] Usage: REQLOAN <AMOUNT> <MONTHS>";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    double loanAmount = Double.parseDouble(parts[1]);
                    int months = Integer.parseInt(parts[2]);

                    return bank.requestLoan(this.currentIban, this.currentPin, loanAmount, months);
                case "PAYLOAN":
                    if(parts.length != 3) {
                        return "[System] Usage: PAYLOAN <LOAN ID> <AMOUNT>";
                    }

                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    String loanId = parts[1];
                    double loanPayment = Double.parseDouble(parts[2]);

                    bank.payLoanInstallment(this.currentIban, this.currentPin, loanId, loanPayment);
                case "LOANINFO":
                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }

                    return bank.getLoans(this.currentIban, this.currentPin);
                case "HELP":
                    return getHelpMessage();
                case "LOGOUT":
                    if(!isLoggedIn()) {
                        return "[System] You are not logged in%n]";
                    }
                    this.currentIban = null;
                    this.currentPin = null;
                    return "[System] Account logged out successfully%n";
                default:
                    return "[System] Unknown command. Type HELP for list with commands.";
            }
        } catch (Exception e) {
            return "[System] ERROR: " + e.getMessage();
        }
    }

    private boolean isLoggedIn() {
        return currentIban != null && currentPin != null;
    }

    private String getHelpMessage() {
        return "[System] BANK COMMANDS:" +
                "01. REGISTER <NAME> <EGN>%n" +
                "02. LOGIN <IBAN> <PIN>%n" +
                "03. DEPOSIT <AMOUNT>%n" +
                "04. WITHDRAW <AMOUNT>%n" +
                "05. TRANSFER <RECEIVER IBAN> <AMOUNT>%n" +
                "06. CHANGEPIN <OLD PIN> <NEW PIN>%n" +
                "07. INFO" +
                "08. REQLOAN <AMOUNT> <MONTHS>%n" +
                "09. PAYLOAN <LOAN ID> <AMOUNT>" +
                "10. LOANINFO" +
                "11. HELP" +
                "12. LOGOUT";
    }
}
