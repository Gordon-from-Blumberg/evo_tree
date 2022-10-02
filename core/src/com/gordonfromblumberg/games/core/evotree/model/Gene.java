package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class Gene implements Poolable {
    private static final Pool<Gene> pool = new Pool<Gene>() {
        @Override
        protected Gene newObject() {
            return new Gene();
        }
    };

    private Gene() {}

    public static Gene getInstance() {
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
