package fmi.yoan.bank.client;

import fmi.yoan.bank.server.Bank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 6262;

    public static  void main(String[] args) {
        System.out.println("[Client] Connected to Server...");

        try(Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("[Client] Connected to Server " + SERVER_IP + ":" + SERVER_PORT);

            PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner consoleInput = new Scanner(System.in);

            Thread listnerThread = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = serverInput.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("[Client] Error: " + e.getMessage());
                }
            });
            listnerThread.start();

            while(true) {
                String cmd = consoleInput.nextLine();
                serverOutput.println(cmd);

                if("EXIT".equalsIgnoreCase(cmd)) {
                    System.out.println("[Client] Exiting...");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[Error] Could not connect to the server." + e.getMessage());
        }
    }
}
