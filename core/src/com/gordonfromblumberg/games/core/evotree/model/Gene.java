package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class Gene {
    final static RandomUtils.RandomGen RAND;

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
            values[i] = (byte) RAND.nextInt(0, 52);
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
