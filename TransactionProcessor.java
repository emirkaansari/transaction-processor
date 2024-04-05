/*
    * Author: Emir Kaan Sari
 */

package com.playtech.assignment;

import com.playtech.assignment.io.reader.BinMappingReader;
import com.playtech.assignment.io.reader.TransactionReader;
import com.playtech.assignment.io.reader.UserReader;
import com.playtech.assignment.io.writer.BalanceWriter;
import com.playtech.assignment.io.writer.EventWriter;
import com.playtech.assignment.pojo.BinMapping;
import com.playtech.assignment.pojo.Event;
import com.playtech.assignment.pojo.Transaction;
import com.playtech.assignment.pojo.User;
import com.playtech.assignment.util.IBANValidator;
import com.playtech.assignment.util.IntervalTree;

import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.*;


public class TransactionProcessor {

    public static final Event SENTINEL = new Event("", "", "");
    private static final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentHashMap<String, String> transactionIds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> accountNumbers = new ConcurrentHashMap<>();
    private static final Set<String> depositAccounts = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws InterruptedException, ExecutionException {


        BinMappingReader binMappingReader = new BinMappingReader(Paths.get(args[2]));
        UserReader userReader = new UserReader(Paths.get(args[0]));
        EventWriter eventWriter = new EventWriter(Paths.get(args[4]), eventQueue);


        Thread file1Thread = new Thread(binMappingReader);
        Thread file2Thread = new Thread(userReader);

        file1Thread.start();
        file2Thread.start();

        file2Thread.join();
        System.out.println("finished reading users file");

        file1Thread.join();
        System.out.println("finished reading bins file");
        IntervalTree binMappingResult = binMappingReader.getResult();
        List<User> users = userReader.getResult();

        TransactionReader transactionReader = new TransactionReader(Paths.get(args[1]), transactionQueue);
        Thread file3Thread = new Thread(transactionReader);
        file3Thread.start();
        Thread eventsFileThread = new Thread(eventWriter);
        eventsFileThread.start();
        processTransactions(binMappingResult, users);

        file3Thread.join();
        System.out.println("finished reading transactions file");

        eventsFileThread.join();
        System.out.println("finished writing events file");

        Thread balancesFileThread = new Thread(new BalanceWriter(Paths.get(args[3]), users));
        balancesFileThread.start();
        balancesFileThread.join();
        System.out.println("finished writing balances file");

    }

    private static void processTransactions(IntervalTree binMappingResult, List<User> users) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Transaction transaction = transactionQueue.poll(500, TimeUnit.MILLISECONDS);
        while (transaction != null) {
            Transaction finalTransaction = transaction;
            Callable<Event> task = () -> processTransaction(users, finalTransaction, binMappingResult);
            Future<Event> future = executor.submit(task);
            eventQueue.offer(future.get());

            //if the queue is empty, wait for 500 milliseconds
            transaction = transactionQueue.poll(500, TimeUnit.MILLISECONDS);
        }
        executor.shutdown();
        eventQueue.offer(SENTINEL);
    }

    private static Event processTransaction(List<User> users, Transaction transaction, IntervalTree binMappingResult) {

        Transaction localTransaction = new Transaction(transaction);
        User localUser = users.stream()
                .filter(u -> u.getId().equals(localTransaction.getUserId()))
                .findFirst()
                .map(User::new)
                .orElse(null);

        try {
            // Check for duplicate transaction ID
            if (transactionIds.putIfAbsent(localTransaction.getId(), "") != null) {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Duplicate transaction ID.");
            }

            if (localUser == null || localUser.getFrozen()) {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "User not found or is frozen.");
            }

            // Validate payment method
            if (localTransaction.getMethod().equals(Transaction.METHOD_TRANSFER)) {
                // Validate the transfer account number's check digit validity
                if (!IBANValidator.isValidIBAN(localTransaction.getAccountNumber(), localUser.getCountry())) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Invalid IBAN.");
                }
            } else if (localTransaction.getMethod().equals(Transaction.METHOD_CARD)) {
                // Only allow debit cards
                String bin = localTransaction.getAccountNumber().substring(0, 10);
                BinMapping binMapping = binMappingResult.search(Long.parseLong(bin));
                if (binMapping == null || !binMapping.getType().equals(BinMapping.TYPE_DC)) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Invalid card number or type.");
                }
                // Confirm that the country of the card matches the user's country
                String userCountryCode3Letter = new Locale("", localUser.getCountry()).getISO3Country();
                if (!binMapping.getCountry().equals(userCountryCode3Letter)) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Card country does not match user country.");
                }
            } else {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Invalid payment method.");
            }

            // Validate that the amount is a valid (positive) number and within deposit/withdraw limits
            if (localTransaction.getType().equals(Transaction.TYPE_WITHDRAW)) {
                if (localTransaction.getAmount() <= 0 || localTransaction.getAmount() < localUser.getWithdrawMin() || localTransaction.getAmount() > localUser.getWithdrawMax()) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Amount out of withdraw limits.");
                }
            }

            // Validate that the amount is a valid (positive) number and within deposit limits
            if (localTransaction.getType().equals(Transaction.TYPE_DEPOSIT)) {
                if (localTransaction.getAmount() <= 0 || localTransaction.getAmount() < localUser.getDepositMin() || localTransaction.getAmount() > localUser.getDepositMax()) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Amount out of deposit limits.");
                }
            }

            // For withdrawals, validate that the user has a sufficient balance for a withdrawal
            if (localTransaction.getType().equals(Transaction.TYPE_WITHDRAW) && localTransaction.getAmount() > localUser.getBalance()) {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Insufficient balance for withdrawal.");
            }

            // Allow withdrawals only with the same payment account that has previously been successfully used for deposit
            if (transaction.getType().equals(Transaction.TYPE_WITHDRAW)) {
                String userId = accountNumbers.get(localTransaction.getAccountNumber());
                if (userId == null || !userId.equals(localTransaction.getUserId()) || !depositAccounts.contains(localTransaction.getAccountNumber())) {
                    return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Cannot withdraw with a new account.");
                }
            }

            // Decline transaction types that aren't deposit or withdrawal
            if (!localTransaction.getType().equals(Transaction.TYPE_DEPOSIT) && !localTransaction.getType().equals(Transaction.TYPE_WITHDRAW)) {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Invalid transaction type.");
            }

            // Users cannot share iban/card; payment account used by one user can no longer be used by another
            String existingUserId = accountNumbers.putIfAbsent(localTransaction.getAccountNumber(), localTransaction.getUserId());
            if (existingUserId != null && !existingUserId.equals(localTransaction.getUserId())) {
                return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Account number used by another user.");
            }

            //Add the account number to the deposit accounts list
            if (localTransaction.getType().equals(Transaction.TYPE_DEPOSIT)) {
                depositAccounts.add(localTransaction.getAccountNumber());
            }

            // Process the transaction
            if (localTransaction.getType().equals(Transaction.TYPE_DEPOSIT)) {
                localUser.addBalance(localTransaction.getAmount());
            } else if (localTransaction.getType().equals(Transaction.TYPE_WITHDRAW)) {
                localUser.subtractBalance(localTransaction.getAmount());
            }

            return new Event(localTransaction.getId(), Event.STATUS_APPROVED, "OK");

        } catch (Exception e) {
            return new Event(localTransaction.getId(), Event.STATUS_DECLINED, "Unexpected error occurred.");
        }
    }
}
