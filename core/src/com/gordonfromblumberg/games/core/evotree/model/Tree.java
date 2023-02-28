package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
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
    private static final Logger log = LogManager.create(Tree.class);

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

    private static final Array<TreePart> newShoots = new Array<>();

    int id;
    int generation;
    int lifetime;
    int age;
    final DNA dna = new DNA();
    int energy;
    Cell root;
    int maxHeight;
    final Array<TreePart> treeParts = new Array<>();
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
            int value = treeLifetime.getValue(i);
            if (value > 0)
                lifetime += value;
        }
        this.lifetime = (lifetime + 1) % (MAX_LIFETIME - MIN_LIFETIME) + MIN_LIFETIME;
        this.age = 0;
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

        if (++age == lifetime) {
            produceSeeds(world);
            return true;
        }

        CellGrid grid = world.getGrid();
        for (TreePart treePart : treeParts) {
            energy += treePart.calcEnergy(grid);
        }

        energy -= getSize() * TreePart.ENERGY_CONSUMPTION;

        if (energy <= 0) {
            log.debug("Tree #" + id + " has no energy and dies");
            return true;
        }

        GeneticRules rules = world.getGeneticRules();
        Iterator<TreePart> it = treeParts.iterator();
        while (it.hasNext()) {
            TreePart part = it.next();
            if (part.type == TreePartType.SHOOT && part.update(grid, newShoots, rules)) {
                it.remove();
                part.release();
            }
        }
        for (TreePart newShoot : newShoots) {
            addPart(newShoot);
        }
        newShoots.clear();

        return false;
    }

    public void addPart(TreePart part) {
        treeParts.add(part);
        part.tree = this;
        if (part.cell.y > maxHeight) {
            maxHeight = part.cell.y;
        }
    }

    private void produceSeeds(EvoTreeWorld world) {
        int shootCount = 0;
        for (TreePart part : treeParts) {
            if (part.type == TreePartType.SHOOT) ++shootCount;
        }
        log.info("Tree #" + id + " attempts to produce seeds: energy=" + energy + ", shoots=" + shootCount);
        if (energy >= shootCount && shootCount > 0) {
            int energyPerSeed = (energy / shootCount) + 1;
            if (energyPerSeed > MAX_ENERGY_PER_SEED) {
                energyPerSeed = MAX_ENERGY_PER_SEED;
            }
            int nextGeneration = generation + 1;
            for (TreePart part : treeParts) {
                if (part.type == TreePartType.SHOOT) {
                    Seed seed = Seed.getInstance();
                    seed.setCell(part.cell);
                    seed.generation = nextGeneration;
                    seed.dna.set(this.dna);
                    seed.dna.mutate();
                    seed.energy = energyPerSeed;
                    world.addSeed(seed);
                    log.info("Seed #" + seed.id + " was produced by tree #" + id
                            + " with energy " + energyPerSeed + " of gen " + nextGeneration);
                }
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
        return treeParts.size;
    }

    public Color getColor() {
        return color;
    }

    public int getEnergy() {
        return energy;
    }

    public int getHeight() {
        return maxHeight - root.y + 1;
    }

    public DNA getDna() {
        return dna;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getAge() {
        return age;
    }

    public int getRestLifeTime() {
        return lifetime - age;
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
        age = 0;
        dna.reset();
        energy = 0;
        root = null;
        maxHeight = 0;
        for (TreePart part : treeParts) {
            part.release();
        }
        treeParts.clear();
        color.set(0);
    }
}
