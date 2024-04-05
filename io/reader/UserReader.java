package com.playtech.assignment.io.reader;

import com.playtech.assignment.pojo.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UserReader extends AbstractFileReader<List<User>> {
    public UserReader(Path filePath) {
        super(filePath);
    }

    @Override
    protected List<User> readFile() {
        List<User> users = new ArrayList<>();
        try (Stream<String> lines = Files.lines(getFilePath())) {
            lines.skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 9)
                    .forEach(parts -> {
                        String id = parts[0].trim();
                        String username = parts[1].trim();
                        double balance = Double.parseDouble(parts[2].trim());
                        String country = parts[3].trim();
                        Boolean frozen = Boolean.parseBoolean(parts[4].trim());
                        double depositMin = Double.parseDouble(parts[5].trim());
                        double depositMax = Double.parseDouble(parts[6].trim());
                        double withdrawMin = Double.parseDouble(parts[7].trim());
                        double withdrawMax = Double.parseDouble(parts[8].trim());

                        User user = new User(id, username, balance, country, frozen, depositMin, depositMax, withdrawMin, withdrawMax);
                        users.add(user);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}
