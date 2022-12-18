package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.Gdx;

public class Logger {
    private final Class<?> clazz;

    Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void trace(String message) {
        log(message, LogLevel.TRACE);
    }

    public void debug(String message) {
        log(message, LogLevel.DEBUG);
    }

    public void info(String message) {
        log(message, LogLevel.INFO);
    }

    public void warn(String message) {
        log(message, LogLevel.WARN);
    }

    public void error(String message) {
        log(message, LogLevel.ERROR);
    }

    public void fatal(String message) {
        log(message, LogLevel.FATAL);
    }

    void log(String message, LogLevel level) {
        LogEntry logEntry = LogEntry.getInstance();
        logEntry.level = level;
        logEntry.timestamp = System.currentTimeMillis();
        logEntry.frameId = Gdx.graphics.getFrameId();
        logEntry.clazz = this.clazz;
        logEntry.message = message;
        LogManager.queue.put(logEntry);
    }
}
