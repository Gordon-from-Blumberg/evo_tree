package com.gordonfromblumberg.games.core.common.log;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.AtomicPool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class LogEntry implements Poolable {
    private static final AtomicPool<LogEntry> pool = new AtomicPool<>(LogEntry::new, AbstractFactory.getInstance()
            .configManager().getInteger("log.capacity"));
    private static int nextId = 1;

    final int id;
    LogLevel level;
    long timestamp;
    long frameId;
    Class<?> clazz;
    volatile String message;

    private LogEntry() {
        id = nextId++;
    }

    public static LogEntry getInstance() {
        return pool.obtain();
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        level = null;
        timestamp = 0;
        frameId = 0;
        clazz = null;
        message = null;
    }
}
