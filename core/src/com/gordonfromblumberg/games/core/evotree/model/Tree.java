package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
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

    private static final int MAX_ENERGY_PER_SEED;
    private static final float MIN_COLOR_VALUE;
    private static final float MAX_COLOR_VALUE;
    private static final int MIN_LIFETIME;
    private static final int MAX_LIFETIME;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MAX_ENERGY_PER_SEED = configManager.getInteger("tree.maxEnergyPerSeed");
        MIN_COLOR_VALUE = configManager.getFloat("tree.minColor");
        MAX_COLOR_VALUE = configManager.getFloat("tree.maxColor");
        MIN_LIFETIME = configManager.getInteger("tree.minLifetime");
        MAX_LIFETIME = configManager.getInteger("tree.maxLifetime");
    }

    int id;
    int generation;
    int lifetime;
    final DNA dna = new DNA();
    int energy;
    final Array<Wood> woods = new Array<>();
    final Array<Shoot> shoots = new Array<>();
    final Array<Shoot> newShoots = new Array<>();
    private final Color color = new Color();

    boolean justSprouted;

    private Tree() {}

    public static Tree getInstance() {
        return pool.obtain();
    }

    public void init() {
        Gene gene = dna.getSpecialGene(DNA.COLOR);
        float clrDiff = MAX_COLOR_VALUE - MIN_COLOR_VALUE;
        color.set(
                MIN_COLOR_VALUE + clrDiff * (gene.getValue(0) ^ gene.getValue(3)) / Gene.MAX_VALUE,
                MIN_COLOR_VALUE + clrDiff * (gene.getValue(1) ^ gene.getValue(3)) / Gene.MAX_VALUE,
                MIN_COLOR_VALUE + clrDiff * (gene.getValue(2) ^ gene.getValue(3)) / Gene.MAX_VALUE,
                1
        );
        Gene treeLifetime = dna.getSpecialGene(DNA.LIFETIME);
        int lifetime = 0;
        for (int i = 0; i < Gene.VALUE_COUNT; ++i) {
            lifetime += treeLifetime.getValue(i);
        }
        this.lifetime = (lifetime - MIN_LIFETIME + 1) % (MAX_LIFETIME - MIN_LIFETIME) + MIN_LIFETIME;
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

        if (--lifetime < 0) {
            produceSeeds(world);
            return true;
        }

        CellGrid grid = world.getGrid();
        for (Wood wood : woods) {
            energy += wood.calcEnergy(grid);
        }
        for (Shoot shoot : shoots) {
            energy += shoot.calcEnergy(grid);
        }

        energy -= getSize() * Wood.ENERGY_CONSUMPTION;

        if (energy <= 0) {
            Gdx.app.log("TREE", "Tree #" + id + " has no energy and dies");
            return true;
        }

        Iterator<Shoot> it = shoots.iterator();
        while (it.hasNext()) {
            Shoot shoot = it.next();
            if (shoot.update(grid, newShoots)) {
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
            if (energyPerSeed > MAX_ENERGY_PER_SEED) {
                energyPerSeed = MAX_ENERGY_PER_SEED;
            }
            int nextGeneration = generation + 1;
            for (Shoot shoot : shoots) {
                Seed seed = Seed.getInstance();
                seed.setCell(shoot.cell);
                seed.generation = nextGeneration;
                seed.dna.set(this.dna);
                seed.dna.mutate();
                seed.energy = energyPerSeed;
                world.addSeed(seed);
                Gdx.app.log("SEED", "Seed #" + seed.id + " was produced by tree #" + id
                        + " with energy " + energyPerSeed + " of gen " + nextGeneration);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return woods.size + shoots.size;
    }

    public Color getColor() {
        return color;
    }

    public int getEnergy() {
        return energy;
    }

    public DNA getDna() {
        return dna;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        id = 0;
        generation = 0;
        lifetime = 0;
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
        color.set(0);
    }
}
