package com.playtech.assignment.io.writer;

import com.playtech.assignment.pojo.Event;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

import static com.playtech.assignment.TransactionProcessor.SENTINEL;

public class EventWriter extends AbstractFileWriter {

    private final BlockingQueue<Event> eventQueue;

    public EventWriter(Path filePath, BlockingQueue<Event> eventQueue) {
        super(filePath);
        this.eventQueue = eventQueue;
    }

    @Override
    protected void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            writer.write("TRANSACTION_ID,STATUS,MESSAGE\n");
            Event event = eventQueue.take();
            while (event != SENTINEL) {
                writer.write(event.getTransactionId() + "," + event.getStatus() + "," + event.getMessage() + "\n");
                event = eventQueue.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
