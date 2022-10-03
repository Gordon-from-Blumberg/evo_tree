package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class Gene {
    final static RandomUtils.RandomGen RAND = RandomUtils.randomGen(-15615);

    private final byte[] values = new byte[4];

    Gene() {}

    void setRandom() {
        for (int i = 0; i < 4; ++i) {
            values[i] = (byte) RAND.nextInt(0, 32);
        }
    }

    void set(Gene other) {
        System.arraycopy(other.values, 0, this.values, 0, 4);
    }

    byte getValue(Direction direction) {
        return values[direction.getCode()];
    }

    void reset() {
        for (int i = 0; i < 4; ++i) {
            values[i] = -1;
        }
    }
}
