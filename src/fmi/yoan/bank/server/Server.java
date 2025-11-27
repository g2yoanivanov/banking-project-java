package fmi.yoan.bank.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORT = 6262;

   public static void main(String[] args) {
       Bank bank = new Bank("YoanBank", "YB", 10000000.00);

       System.out.println("[System] Bank created successfully!");
       System.out.println("Bank reserves: " + bank.getBankReserves());

        startInterestTimer(bank);

        ExecutorService clientThreadPool = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket((PORT))) {
            System.out.println("[System] Server listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[System] Accepted connection from " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, bank);

                clientThreadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("[Error] " +  e.getMessage());
            e.printStackTrace();
        }
   }

   private static void startInterestTimer(Bank bank) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable interestTask = () -> {
            try {
                bank.runDailyPenaltyCheck();
            } catch (Exception e) {
                System.err.println("[Error] " +  e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(interestTask,10, 60, TimeUnit.SECONDS);

        System.out.println("[System] Penalty check started! (1 min = 1 day");
   }
}
