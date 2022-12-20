package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.AtomicQueue;

import java.io.IOException;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.padLeft;

public class LogManager {
    static final AtomicQueue<LogEntry> queue = new AtomicQueue<>(16);
    static final Array<LogAppender> appenders = new Array<>(false, 4);
    static Thread logThread;

    private static final long MILLIS_MOD = 100_000_000;
    private static final Logger log = create(LogManager.class);
    private static final StringBuilder stringBuilder = new StringBuilder();

    public static void init() {
        Thread thread = new Thread("Logger") {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    LogEntry entry = queue.poll();

                    if (entry != null) {
                        String message = composeMessage(entry);
                        for (LogAppender appender : appenders) {
                            if (entry.level.ordinal() >= appender.getMinLevel()) {
                                appender.append(message, entry);
                            }
                        }
                        entry.release();
                    }

                    Thread.yield();
                }

                for (LogAppender appender : appenders) {
                    try {
                        appender.close();
                    } catch (IOException e) {
                        System.err.println("Error while closing appender " + appender.getClass().getSimpleName());
                    }
                }
            }
        };
        thread.setDaemon(true);

        StdOutLogAppender stdOutLogAppender = new StdOutLogAppender();
        appenders.add(stdOutLogAppender);

        thread.start();
        logThread = thread;
        log.info("Logger thread started");
    }

    public static Logger create(Class<?> clazz) {
        return new Logger(clazz);
    }

    public static void addAppender(LogAppender appender) {
        appenders.add(appender);
    }

    public static void close() {
        log.info("Close LogManager");
        logThread.interrupt();
    }

    private static String composeMessage(LogEntry entry) {
        StringBuilder sb = stringBuilder;
        try {
            sb.append('[').append(entry.level.name()).append(']');
            sb.append('\t');

            sb.append(padLeft(entry.frameId, 6));
            sb.append('\t');

            long time = entry.timestamp % MILLIS_MOD;
            sb.append(padLeft(time / 1_000_000, 2)).append('_');
            time %= 1_000_000;
            sb.append(padLeft(time / 1_000, 3)).append('_');
            time %= 1_000;
            sb.append(padLeft(time, 3));
            sb.append(' ');

            sb.append(entry.clazz.getSimpleName());
            sb.append(' ');

            sb.append(entry.message);
            return sb.toString();
        } finally {
            sb.delete(0, sb.length());
        }
    }
}
