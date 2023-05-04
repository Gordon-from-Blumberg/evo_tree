package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.utils.Pool;

import java.util.Objects;

public class IntVector2 implements Pool.Poolable {
    public int x;
    public int y;

    public IntVector2() {}

    public IntVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntVector2)) return false;
        IntVector2 other = (IntVector2) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
