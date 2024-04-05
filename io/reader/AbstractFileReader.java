package com.playtech.assignment.io.reader;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractFileReader<T> implements Runnable {

    private final Path filePath;
    private T result;

    public AbstractFileReader(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            result = readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getFilePath() {
        return filePath;
    }

    public T getResult() {
        return result;
    }

    protected abstract T readFile() throws IOException;

}
