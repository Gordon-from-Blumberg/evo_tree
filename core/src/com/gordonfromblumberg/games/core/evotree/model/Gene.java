package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class Gene {
    static final RandomUtils.RandomGen RAND;
    static final int MAX_VALUE = 54;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        RAND = RandomUtils.randomGen(configManager.contains("seed")
                ? configManager.getLong("seed")
                : RandomUtils.nextLong());
    }

    private final byte[] values = new byte[4];

    Gene() {}

    void setRandom() {
        for (int i = 0; i < 4; ++i) {
            values[i] = (byte) RAND.nextInt(0, MAX_VALUE);
        }
    }

    void set(Gene other) {
        System.arraycopy(other.values, 0, this.values, 0, 4);
    }

    byte getValue(Direction direction) {
        return values[direction.getCode()];
    }

    byte getValue(int index) {
        return values[index];
    }

    void reset() {
        for (int i = 0; i < 4; ++i) {
            values[i] = -1;
        }
    }
}
