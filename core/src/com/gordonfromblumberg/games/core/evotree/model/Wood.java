package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;

public class Wood extends TreePart {
    private static final Pool<Wood> pool = new Pool<Wood>() {
        @Override
        protected Wood newObject() {
            return new Wood();
        }
    };

    static final int ENERGY_CONSUMPTION = 8;

    protected Tree tree;

    Wood() {
        super(ENERGY_CONSUMPTION);
    }

    public static Wood getInstance() {
        return pool.obtain();
    }

    public Tree getTree() {
        return tree;
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
        super.reset();

        tree = null;
    }
}
