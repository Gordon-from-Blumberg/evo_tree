package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;

public class Wood extends TreePart {
    private static final Pool<Wood> pool = new Pool<Wood>() {
        @Override
        protected Wood newObject() {
            return new Wood();
        }
    };

    private static final int MIN_ABSORPTION = 4;
    private static final int MAX_ABSORPTION = 50;
    static final int ENERGY_CONSUMPTION = 8;

    protected Tree tree;
    protected int lightAbsorption;

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
        return lightAbsorption;
    }

    protected static int calcLightAbsorption(int geneValue) {
        int absorption = MIN_ABSORPTION + geneValue;
        return absorption > MAX_ABSORPTION ? MAX_ABSORPTION : absorption;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        tree = null;
        lightAbsorption = 0;
    }
}
