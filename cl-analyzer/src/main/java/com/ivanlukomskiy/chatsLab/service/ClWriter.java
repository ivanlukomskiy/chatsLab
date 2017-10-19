package com.ivanlukomskiy.chatsLab.service;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static au.com.bytecode.opencsv.CSVWriter.NO_QUOTE_CHARACTER;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
public class ClWriter implements Closeable {
    private CSVWriter writer;
    public static final char CSV_DELIMITER = ';';

    @SneakyThrows
    public ClWriter(File file) {
        FileWriter fileWriter = new FileWriter(file);
        writer = new CSVWriter(fileWriter, CSV_DELIMITER, NO_QUOTE_CHARACTER);
    }

    @SneakyThrows
    public void write(Object... row) {
        String[] rowString = new String[row.length];
        for(int i = 0; i < row.length; i++ ) {
            rowString[i] = row[i] == null ? "" : row[i].toString();
        }
        writer.writeNext(rowString);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
