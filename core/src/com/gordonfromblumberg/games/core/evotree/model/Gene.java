package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;

public class Gene {
    private static final Logger log = LogManager.create(Gene.class);

    static final RandomGen RAND = RandomGen.INSTANCE;
    static final int MAX_VALUE = 42;
    public static final int LIGHT_ABSORPTION;
    public static final int CONDITION1;
    public static final int PARAMETER1;
    public static final int CONDITION2;
    public static final int PARAMETER2;
    public static final int MOVE_TO;
    static final int VALUE_COUNT;

    static {
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

    public byte getValue(Direction direction) {
        return values[direction.getCode()];
    }

    public byte getValue(int index) {
        return values[index];
    }

    void reset() {
        for (int i = 0; i < 4; ++i) {
            values[i] = -1;
        }
    }
}
