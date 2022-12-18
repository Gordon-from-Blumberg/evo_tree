package com.gordonfromblumberg.games.core.common.log;

public class StdOutLogAppender extends LogAppender {

    @Override
    void append(String message, LogEntry logEntry) {
        if (logEntry.level.ordinal() >= LogLevel.ERROR.ordinal()) {
            System.err.println(message);
        } else {
            System.out.println(message);
        }
    }
}
