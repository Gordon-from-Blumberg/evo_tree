package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;

public class Wood extends TreePart {
    private static final Pool<Wood> pool = new Pool<Wood>() {
        @Override
        protected Wood newObject() {
            return new Wood();
        }
    };

    protected Tree tree;

    Wood() {}

    public static Wood getInstance() {
        return pool.obtain();
    }

    @Override
    public int getLightAbsorption() {
        return 0;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        id = 0;
        cell = null;
        tree = null;
    }
}
