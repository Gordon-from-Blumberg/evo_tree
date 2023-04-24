package com.gordonfromblumberg.games.core.common.chunk;

import com.badlogic.gdx.utils.Pool;

public class ChunkItem<T> implements Pool.Poolable {
    int x;
    int y;
    T object;

    ChunkItem<T> set(int x, int y, T object) {
        this.x = x;
        this.y = y;
        this.object = object;
        return this;
    }

    @Override
    public void reset() {
        x = 0;
        y = 0;
        object = null;
    }
}
