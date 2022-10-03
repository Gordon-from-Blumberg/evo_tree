package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

import java.util.Iterator;

public class Tree implements Poolable {
    private static final Pool<Tree> pool = new Pool<Tree>() {
        @Override
        protected Tree newObject() {
            return new Tree();
        }
    };

    private static final int ENERGY_REQUIRED_TO_SPROUT = 10;

    int id;
    int generation;
    int turnsRemain;
    final DNA dna = new DNA();
    int energy;
    final Array<Wood> woods = new Array<>();
    final Array<Shoot> shoots = new Array<>();
    final Array<Shoot> newShoots = new Array<>();

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
        if (justSprouted) {
            justSprouted = false;
            return false;
        }

        if (--turnsRemain < 0) {
            produceSeeds(world);
            return true;
        }

        CellGrid grid = world.getGrid();
        for (Wood wood : woods) {
            energy += wood.calcLight(grid);
        }
        for (Shoot shoot : shoots) {
            energy += shoot.calcLight(grid);
        }

        energy -= getSize() * Wood.ENERGY_CONSUMPTION;

        if (energy <= 0) {
            Gdx.app.log("TREE", "Tree #" + id + " has no energy and dies");
            return true;
        }

        Iterator<Shoot> it = shoots.iterator();
        while (it.hasNext()) {
            Shoot shoot = it.next();
            int requiredEnergy = ENERGY_REQUIRED_TO_SPROUT * shoot.canMakeChildrenCount(grid);
            if (requiredEnergy < energy) {
                energy -= requiredEnergy;
                shoot.sprout(world, newShoots);
                it.remove();
                shoot.release();
            }
        }
        for (Shoot newShoot : newShoots) {
            addShoot(newShoot);
        }
        newShoots.clear();

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

    private void produceSeeds(EvoTreeWorld world) {
        Gdx.app.log("TREE", "Tree #" + id + " attempts to produce seeds: energy=" + energy + ", shoots=" + shoots.size);
        if (energy >= shoots.size && shoots.size > 0) {
            int energyPerSeed = (energy / shoots.size) + 1;
            int nextGeneration = generation + 1;
            for (Shoot shoot : shoots) {
                Seed seed = Seed.getInstance();
                seed.setCell(shoot.cell);
                seed.generation = nextGeneration;
                seed.dna.set(this.dna);
                seed.dna.mutate();
                seed.energy = energyPerSeed;
                world.addSeed(seed);
            }
        }
    }

    public int getSize() {
        return woods.size + shoots.size;
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
