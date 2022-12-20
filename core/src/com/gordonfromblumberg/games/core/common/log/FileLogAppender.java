package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.Writer;

public class FileLogAppender extends LogAppender {
    private static final char NEW_LINE = '\n';
    private final String logFile;
    private final Writer writer;

    public FileLogAppender(FileHandle file) {
        logFile = file.path();
        writer = file.writer(true);
        try {
            printInit();
        } catch (IOException e) {
            System.err.println("Can not write to file " + logFile + ": " + e);
        }
    }

    @Override
    void append(String message, LogEntry logEntry) {
        try {
            writer.write(message);
            writer.write(NEW_LINE);
        } catch (IOException e) {
            System.err.println("IOException when writing to log file " + logFile + ": " + e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private void printInit() throws IOException {
        writer.append(NEW_LINE)
                .append("NEW SESSION")
                .append(NEW_LINE);
    }
}
