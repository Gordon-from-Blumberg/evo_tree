package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.AtomicQueue;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.DateTimeFormatter;

import java.io.IOException;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.padLeft;

public class LogManager {
    static AtomicQueue<LogEntry> queue;
    static final Array<LogAppender> appenders = new Array<>(false, 4);
    static Thread logThread;

    private static final Logger log = create(LogManager.class);
    private static final StringBuilder stringBuilder = new StringBuilder();
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(true);

    public static void init() {
        queue = new AtomicQueue<>(AbstractFactory.getInstance()
                .configManager().getInteger("log.capacity"));

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
            sb.append(padLeft(entry.frameId, 6));
            sb.append('\t');

            sb.append(dateTimeFormatter.format(entry.timestamp));
            sb.append('\t');

            sb.append('[').append(entry.level.name()).append(']');
            sb.append(' ');

            sb.append('{').append(entry.clazz.getSimpleName()).append('}');
            sb.append(' ');

            sb.append(entry.message);
            return sb.toString();
        } finally {
            sb.delete(0, sb.length());
        }
    }
}
