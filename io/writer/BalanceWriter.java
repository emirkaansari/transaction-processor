package com.playtech.assignment.io.writer;

import com.playtech.assignment.pojo.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;


public class BalanceWriter extends AbstractFileWriter {

    private final List<User> users;

    public BalanceWriter(Path filePath, List<User> users) {
        super(filePath);
        this.users = users;
    }

    @Override
    protected void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            writer.write("USER_ID,BALANCE\n");
            for (User user : users) {
                writer.write(user.getId() + "," + String.format("%.2f", user.getBalance()) + "\n");
            }
        }
    }
}
