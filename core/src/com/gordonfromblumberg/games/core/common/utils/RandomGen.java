package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.RandomXS128;

import java.util.Random;

public class RandomGen {
    public static final RandomGen INSTANCE = new RandomGen();

    private final Random rand = new RandomXS128();

    public static void setSeed(long seed) {
        INSTANCE.rand.setSeed(seed);
    }

    public int nextInt(int to) {
        return rand.nextInt(to);
    }

    public int nextInt(int from, int to) {
        return from == to ? from : from + rand.nextInt(to - from);
    }

    public float nextFloat() {
        return rand.nextFloat();
    }

    public float nextFloat(float max) {
        return max * rand.nextFloat();
    }

    public float nextFloat(float from, float to) {
        return from == to ? from : from + ((to - from) * rand.nextFloat());
    }

    public long nextLong() {
        return rand.nextLong();
    }

    public boolean nextBool() {
        return rand.nextBoolean();
    }

    public boolean nextBool(float chance) {
        return rand.nextFloat() < chance;
    }
}
