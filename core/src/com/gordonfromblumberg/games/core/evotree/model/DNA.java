package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class DNA implements Poolable {
    private static final Pool<DNA> pool = new Pool<DNA>() {
        @Override
        protected DNA newObject() {
            return new DNA();
        }
    };

    Gene[] genes;

    private DNA() {}

    public static DNA getInstance() {
        return pool.obtain();
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {

    }
}
