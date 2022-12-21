package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.utils.AtomicQueue;

import java.util.function.Supplier;

public class AtomicPool<T extends Poolable> {
    private static final int DEFAULT_CAPACITY = 32;

    private final Supplier<T> supplier;
    private final AtomicQueue<T> queue;

    public AtomicPool(Supplier<T> supplier) {
        this(supplier, DEFAULT_CAPACITY);
    }

    public AtomicPool(Supplier<T> supplier, int capacity) {
        this.supplier = supplier;
        this.queue = new AtomicQueue<>(capacity);
    }

    public T obtain() {
        T item = queue.poll();
        return item == null ? supplier.get() : item;
    }

    public void free(T item) {
        item.reset();
        queue.put(item);
    }
}
