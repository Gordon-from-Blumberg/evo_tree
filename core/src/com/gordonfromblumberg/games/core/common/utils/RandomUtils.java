package com.gordonfromblumberg.games.core.common.utils;

import java.util.Random;

public class RandomUtils {
    private static final Random RAND = new Random();

    public static int nextInt(int to) {
        return RAND.nextInt(to);
    }

    public static int nextInt(int from, int to) {
        return from == to ? from : from + RAND.nextInt(to - from);
    }

    public static float nextFloat() {
        return RAND.nextFloat();
    }

    public static float nextFloat(float max) {
        return max * RAND.nextFloat();
    }

    public static float nextFloat(float from, float to) {
        return from == to ? from : from + ((to - from) * RAND.nextFloat());
    }

    public static long nextLong() {
        return RAND.nextLong();
    }

    public static boolean nextBool() {
        return RAND.nextFloat() < 0.5f;
    }

    public static boolean nextBool(float chance) {
        return RAND.nextFloat() < chance;
    }

    public static RandomGen randomGen(long seed) {
        return new RandomGen(seed);
    }

    public static class RandomGen {
        private final Random rand;

        private RandomGen(long seed) {
            rand = new Random(seed);
        }

        public int nextInt(int from, int to) {
            return from == to ? from : from + rand.nextInt(to - from);
        }

        public float nextFloat(float from, float to) {
            return from == to ? from : from + ((to - from) * rand.nextFloat());
        }

        public boolean nextBool(float chance) {
            return rand.nextFloat() < chance;
        }
    }
}
