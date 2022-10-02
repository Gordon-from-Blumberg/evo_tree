package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;

public class Shoot extends Wood {
    private static final Pool<Shoot> pool = new Pool<Shoot>() {
        @Override
        protected Shoot newObject() {
            return new Shoot();
        }
    };

    Gene activeGene;

    private Shoot() {}

    public static Shoot getInstance() {
        return pool.obtain();
    }

    public void sprout() {

    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        activeGene = null;
    }
}
