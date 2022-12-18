package com.gordonfromblumberg.games.core.common.log;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class LogEntry implements Poolable {
    private static final Pool<LogEntry> pool = new Pool<LogEntry>() {
        @Override
        protected LogEntry newObject() {
            return new LogEntry();
        }
    };
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
