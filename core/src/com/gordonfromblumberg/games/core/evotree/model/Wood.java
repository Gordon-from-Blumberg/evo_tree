package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class Wood extends TreePart {
    private static final Pool<Wood> pool = new Pool<Wood>() {
        @Override
        protected Wood newObject() {
            return new Wood();
        }
    };

    protected Tree tree;

    Wood() {
        super(8);
    }

    public static Wood getInstance() {
        return pool.obtain();
    }

    @Override
    public boolean update(EvoTreeWorld world) {
        tree.energy -= energyConsumption;
        return tree.energy <= 0;
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
        tree = null;
    }
}
