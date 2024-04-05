package com.playtech.assignment.io.writer;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractFileWriter implements Runnable {

    private final Path filePath;

    protected AbstractFileWriter(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            writeToFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void writeToFile(Path filePath) throws IOException;
}
