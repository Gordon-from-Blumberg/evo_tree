package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class Tree implements Poolable {
    private static final Pool<Tree> pool = new Pool<Tree>() {
        @Override
        protected Tree newObject() {
            return new Tree();
        }
    };

    int id;
    int generation;
    int turnsRemain;
    DNA dna;
    int energy;
    final Array<TreePart> parts = new Array<>();

    private Tree() {}

    public static Tree getInstance() {
        return pool.obtain();
    }

    public void update() {

    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        id = 0;
        generation = 0;
        turnsRemain = 0;
        dna.release();
        dna = null;
        energy = 0;
        for (TreePart part : parts) {
            part.release();
        }
        parts.clear();
    }
}
