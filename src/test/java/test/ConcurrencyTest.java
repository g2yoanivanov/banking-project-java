package test;

import fmi.yoan.bank.server.Account;
import fmi.yoan.bank.server.Bank;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyTest {

    @Test
    void testConcurrentWithdrawal() throws InterruptedException {
        Account account = new Account("Test Test", "1010101010",
                "BG62G2YB00000000000001", "1234", 100.00);

        int numOfThreads = 2;
        ExecutorService service =  Executors.newFixedThreadPool(numOfThreads);

        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        for (int i = 0; i < numOfThreads; i++) {
            service.execute(() -> {
                try {
                    latch.await();

                    account.withdraw(100);

                    success.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                }
            });
        }

        latch.countDown();

        Thread.sleep(1000);
        service.shutdown();

        assertEquals(1, success.get(), "Only 1 succeeded");
        assertEquals(1, failed.get(), "Only 1 failed");
        assertEquals(0.00, account.getBalance());
    }

    @Test
    void testConcurrentTransfer() throws InterruptedException {
        Bank bank =  new Bank("YoanBank", "G2YB", 10000.00);

        String sender = bank.registerAccount("Sender", "1010101010");
        String receiver = bank.registerAccount("Receiver", "2020202020");

        String senderIban = sender
                .split("IBAN: ")[1].split("\\n")[0].trim();

        String receiverIban = receiver
                .split("IBAN: ")[1].split("\\n")[0].trim();

        String senderPin = sender.split("PIN: ")[1].split("\\n")[0].trim();;
        String receiverPin = receiver.split("PIN: ")[1].split("\\n")[0].trim();

        bank.deposit(senderIban, senderPin, 1000);
        bank.deposit(receiverIban, receiverPin, 1000);

        int numOfThreads = 20;
        ExecutorService service =  Executors.newFixedThreadPool(numOfThreads);

        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < numOfThreads; i++) {
            service.execute(() -> {
                try {
                    latch.await();

                    bank.transfer(senderIban, receiverIban, senderPin, 10);
                    bank.transfer(receiverIban, senderIban, receiverPin, 10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        Thread.sleep(2000);
        service.shutdown();

        double balance1 = bank.getBalance(senderIban);
        double balance2 = bank.getBalance(receiverIban);

        assertEquals(2000.00, balance2 + balance1, 0.0001, "Money should not disappear");
    }
}
