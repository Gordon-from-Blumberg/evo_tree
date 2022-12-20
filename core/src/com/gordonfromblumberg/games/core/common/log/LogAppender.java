package com.gordonfromblumberg.games.core.common.log;

import java.io.IOException;

public abstract class LogAppender implements AutoCloseable {

    LogLevel minLevel = LogLevel.DEBUG;

    int getMinLevel() {
        return minLevel.ordinal();
    }

    abstract void append(String message, LogEntry logEntry);

    @Override
    public abstract void close() throws IOException;
}
