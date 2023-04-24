package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.utils.Pool;

public class IntVector2 implements Pool.Poolable {
    public int x;
    public int y;

    public IntVector2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public void reset() {
        x = 0;
        y = 0;
    }
}
