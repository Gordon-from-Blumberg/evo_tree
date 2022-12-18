package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.AtomicQueue;

public class LogManager {
    static final AtomicQueue<LogEntry> queue = new AtomicQueue<>(16);
    static final Array<LogAppender> appenders = new Array<>(false, 4);
    static Thread logThread;

    private final static Logger log = create(LogManager.class);

    public static void init() {
        Thread thread = new Thread("Logger") {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    LogEntry entry = queue.poll();
                    if (entry != null) {
                        for (LogAppender appender : appenders) {
                            appender.append(entry);
                        }

                        entry.release();
                    }

                    Thread.yield();
                }
            }
        };
        thread.setDaemon(true);

        StdOutLogAppender stdOutLogAppender = new StdOutLogAppender();
        appenders.add(stdOutLogAppender);

        thread.start();
        log.info("Logger thread started");
    }

    public static Logger create(Class<?> clazz) {
        return new Logger(clazz);
    }
}
