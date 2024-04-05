package com.playtech.assignment.io.reader;

import com.playtech.assignment.pojo.BinMapping;
import com.playtech.assignment.util.IntervalTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class BinMappingReader extends AbstractFileReader<IntervalTree> {

    public BinMappingReader(Path filePath) {
        super(filePath);
    }

    @Override
    protected IntervalTree readFile() {
        IntervalTree intervalTree = new IntervalTree();
        try (Stream<String> lines = Files.lines(getFilePath())) {
            lines.parallel()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 5)
                    .forEach(parts -> {
                        String name = parts[0].trim();
                        long rangeFrom = Long.parseLong(parts[1].trim());
                        long rangeTo = Long.parseLong(parts[2].trim());
                        String type = parts[3].trim();
                        String country = parts[4].trim();

                        BinMapping binMapping = new BinMapping(name, rangeFrom, rangeTo, type, country);
                        intervalTree.insert(binMapping);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return intervalTree;
    }
}
