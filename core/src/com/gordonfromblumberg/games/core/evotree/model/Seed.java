package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class Seed extends TreePart {
    private static final Pool<Seed> pool = new Pool<Seed>() {
        @Override
        protected Seed newObject() {
            return new Seed();
        }
    };

    private static final int ENERGY_REQUIRED_TO_SPROUT = 10;

    private static final int MIN_LIGHT_TO_SPROUT;
    private static final int MAX_LIGHT_TO_SPROUT;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MIN_LIGHT_TO_SPROUT = configManager.getInteger("seed.minLightToSprout");
        MAX_LIGHT_TO_SPROUT = configManager.getInteger("seed.maxLightToSprout");
    }

    int id;
    int generation;
    final DNA dna = new DNA();
    int energy;
    int lightToSprout;

    private Seed() {
        super(4);
    }

    public static Seed getInstance() {
        return pool.obtain();
    }

    public void init() {
        Gene lightToSproutGene = dna.getGene(DNA.SEED_SPROUT_LIGHT);
        int lightToSprout = 0;
        for (int i = 0; i < 4; ++i) {
            lightToSprout += lightToSproutGene.getValue(i);
        }
        this.lightToSprout = (lightToSprout - MIN_LIGHT_TO_SPROUT + 1) % (MAX_LIGHT_TO_SPROUT - MIN_LIGHT_TO_SPROUT) + MIN_LIGHT_TO_SPROUT;
    }

    @Override
    public boolean update(EvoTreeWorld world) {
        energy -= energyConsumption;
        if (energy <= 0) {
            return true;
        }

        CellGrid grid = world.getGrid();
        if (cell.y > 0) {
            Cell next = cell;
            for (int i = 0; i < 3; ++i) {
                Cell c = grid.getCell(next, Direction.down);
                if (c != null && c.treePart == null) {
                    next = c;
                } else {
                    break;
                }
            }
            if (cell != next) {
                cell.treePart = null;
                setCell(next);
            }
            return false;
        }

        if (energy > ENERGY_REQUIRED_TO_SPROUT
                && calcLight(grid) >= lightToSprout
                && !(grid.getCell(cell, Direction.left).treePart instanceof Wood)
                && !(grid.getCell(cell, Direction.right).treePart instanceof Wood)) {
            energy -= ENERGY_REQUIRED_TO_SPROUT;
            sprout(world);
            return true;
        }
        return false;
    }

    private void sprout(EvoTreeWorld world) {
        Tree tree = Tree.getInstance();
        tree.generation = this.generation;
        tree.dna.set(this.dna);
        tree.init();
        tree.energy = this.energy;
        Shoot shoot = Shoot.getInstance();
        shoot.setCell(this.cell);
        shoot.activeGene = tree.dna.getGene(0);
        shoot.lightAbsorption = Wood.calcLightAbsorption(shoot.activeGene.getValue(Gene.LIGHT_ABSORPTION));
        tree.addShoot(shoot);
        tree.justSprouted = true;
        world.addTree(tree);
        Gdx.app.log("TREE", "Tree #" + tree.id + " was sprouted from seed #" + id + " with energy " + tree.energy);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public int getLightAbsorption() {
        return 1000;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        id = 0;
        generation = 0;
        dna.reset();
        energy = 0;
        lightToSprout = -1;
    }
}
