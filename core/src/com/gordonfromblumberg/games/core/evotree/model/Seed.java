package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;

public class Seed extends TreePart {
    private static final Pool<Seed> pool = new Pool<Seed>() {
        @Override
        protected Seed newObject() {
            return new Seed();
        }
    };

    int generation;
    DNA dna;
    int energy;

    private Seed() {}

    public static Seed getInstance() {
        return pool.obtain();
    }

    public void sprout() {

    }

    @Override
    public int getLightAbsorption() {
        return 100;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        id = 0;
        cell = null;
        generation = 0;
        dna = null;
        energy = 0;
    }
}
