package com.playtech.assignment.io.reader;

import com.playtech.assignment.pojo.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public class TransactionReader extends AbstractFileReader<Transaction> {

    private final BlockingQueue<Transaction> queue;

    public TransactionReader(Path filePath, BlockingQueue<Transaction> queue) {
        super(filePath);
        this.queue = queue;
    }

    @Override
    protected Transaction readFile() {
        try (Stream<String> lines = Files.lines(getFilePath())) {
            lines.skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 6)
                    .forEach(parts -> {
                        String id = parts[0].trim();
                        String userId = parts[1].trim();
                        String type = parts[2].trim();
                        double amount = Double.parseDouble(parts[3].trim());
                        String method = parts[4].trim();
                        String accountNumber = parts[5].trim();

                        Transaction transaction = new Transaction(id, userId, type, amount, method, accountNumber);
                        queue.offer(transaction);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
