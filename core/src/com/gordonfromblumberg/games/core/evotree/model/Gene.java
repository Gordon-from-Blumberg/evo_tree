package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class Gene {
    static final RandomUtils.RandomGen RAND;
    static final int MAX_VALUE = 42;
    static final int LIGHT_ABSORPTION;
    static final int CONDITION1;
    static final int PARAMETER1;
    static final int CONDITION2;
    static final int PARAMETER2;
    static final int MOVE_TO;
    static final int VALUE_COUNT;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        long seed = configManager.contains("seed")
                ? configManager.getLong("seed")
                : RandomUtils.nextLong();
        Gdx.app.log("RANDOM", "Seed = " + seed);
        RAND = RandomUtils.randomGen(seed);
        int valueCount = Direction.ALL.length;
        LIGHT_ABSORPTION = valueCount++;
        CONDITION1 = valueCount++;
        PARAMETER1 = valueCount++;
        CONDITION2 = valueCount++;
        PARAMETER2 = valueCount++;
        MOVE_TO = valueCount++;
        VALUE_COUNT = valueCount;
    }

    private final byte[] values = new byte[VALUE_COUNT];

    Gene() {}

    void setRandom() {
        for (int i = 0; i < VALUE_COUNT; ++i) {
            values[i] = (byte) RAND.nextInt(0, MAX_VALUE);
        }
    }

    void mutate() {
        values[RAND.nextInt(0, 4)] = (byte) RAND.nextInt(0, MAX_VALUE);
    }

    void set(Gene other) {
        System.arraycopy(other.values, 0, this.values, 0, VALUE_COUNT);
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
