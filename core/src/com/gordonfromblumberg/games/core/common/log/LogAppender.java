package com.gordonfromblumberg.games.core.common.log;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.*;

public abstract class LogAppender {
    private static final long MILLIS_MOD = 100_000_000;

    LogLevel minLevel = LogLevel.DEBUG;
    private final StringBuilder stringBuilder = new StringBuilder();

    void append(LogEntry logEntry) {
        if (logEntry.level.ordinal() >= minLevel.ordinal()) {
            stringBuilder.append('[').append(logEntry.level.name()).append(']');
            stringBuilder.append('\t');

            stringBuilder.append(padLeft(logEntry.frameId, 6));
            stringBuilder.append('\t');

            long time = logEntry.timestamp % MILLIS_MOD;
            stringBuilder.append(padLeft(time / 1_000_000, 2)).append('_');
            time %= 1_000_000;
            stringBuilder.append(padLeft(time / 1_000, 3)).append('_');
            time %= 1_000;
            stringBuilder.append(padLeft(time, 3));
            stringBuilder.append(' ');

            stringBuilder.append(logEntry.clazz.getSimpleName());
            stringBuilder.append(' ');

            stringBuilder.append(logEntry.message);

            append(stringBuilder.toString(), logEntry);
            stringBuilder.delete(0, stringBuilder.length());
        }
    }

    abstract void append(String message, LogEntry logEntry);
}
