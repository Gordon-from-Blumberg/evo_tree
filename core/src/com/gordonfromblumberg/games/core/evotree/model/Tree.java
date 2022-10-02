package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

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
    final DNA dna = new DNA();
    int energy;
    final Array<Wood> woods = new Array<>();
    final Array<Shoot> shoots = new Array<>();

    boolean justSprouted;

    private Tree() {}

    public static Tree getInstance() {
        return pool.obtain();
    }

    /**
     * Updates state of this tree
     * @param world Game world
     * @return true if this tree should be removed from world
     */
    public boolean update(EvoTreeWorld world) {
        --turnsRemain;
        return false;
    }

    public void addShoot(Shoot shoot) {
        shoots.add(shoot);
        shoot.tree = this;
    }

    public void addWood(Wood wood) {
        if (wood instanceof Shoot) {
            throw new IllegalArgumentException("Shoot should be added as shoot");
        }
        woods.add(wood);
        wood.tree = this;
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
        dna.reset();
        energy = 0;
        for (Wood wood : woods) {
            wood.release();
        }
        woods.clear();
        for (Shoot shoot : shoots) {
            shoot.release();
        }
        shoots.clear();
    }
}
