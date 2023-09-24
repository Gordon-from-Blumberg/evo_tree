package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class Seed extends LivingCellObject {
    private static final Pool<Seed> pool = new Pool<Seed>() {
        @Override
        protected Seed newObject() {
            return new Seed();
        }
    };
    private static final Logger log = LogManager.create(Seed.class);

    private static final int ENERGY_REQUIRED_TO_SPROUT = 10;
    private static final int ENERGY_CONSUMPTION = 4;

    private static final int MIN_LIGHT_TO_SPROUT;
    private static final int MAX_LIGHT_TO_SPROUT;

    private enum State {
        WAITING, SPROUTING;
        private int energyConsumption;
    }

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        State.WAITING.energyConsumption = configManager.getInteger("seed.waitingEnergyConsumption");
        State.SPROUTING.energyConsumption = configManager.getInteger("seed.sproutingEnergyConsumption");
        MIN_LIGHT_TO_SPROUT = configManager.getInteger("seed.minLightToSprout");
        MAX_LIGHT_TO_SPROUT = configManager.getInteger("seed.maxLightToSprout");
    }

    int id;
    int generation;
    final DNA dna = new DNA();
    int energy;
    int lightToSprout;
    private State state;
    private int turnsToSprout;

    private Seed() {}

    public static Seed getInstance() {
        return pool.obtain();
    }

    public void init() {
        Gene lightToSproutGene = dna.getSpecialGene(DNA.SEED_SPROUT_LIGHT);
        int lightToSprout = 0;
        for (int i = 0; i < Gene.VALUE_COUNT; ++i) {
            lightToSprout += lightToSproutGene.getValue(i);
        }
        if (lightToSprout < 0) lightToSprout = 0;
        this.lightToSprout = (lightToSprout + 1) % (MAX_LIGHT_TO_SPROUT - MIN_LIGHT_TO_SPROUT) + MIN_LIGHT_TO_SPROUT;
        this.state = State.WAITING;
        this.turnsToSprout = RandomGen.INSTANCE.nextInt(4, 10);
    }

    public void initRandom() {
        dna.setRandom();
        init();
    }

    @Override
    public boolean update(EvoTreeWorld world) {
        energy -= state.energyConsumption;
        if (energy <= 0) {
            return true;
        }

        CellGrid grid = world.getGrid();
        if (cell.y > 0) {
            Cell next = cell;
            for (int i = 0; i < 3; ++i) {
                Cell c = grid.getCell(next, Direction.down);
                if (c != null && c.object == null) {
                    next = c;
                } else {
                    break;
                }
            }
            if (cell != next) {
                grid.moveCellObjectTo(this, next);
            }
            return false;
        } else if (state == State.WAITING && calcLight(grid) > 0) {
            state = State.SPROUTING;
        }

        if (calcLight(grid) >= lightToSprout) {
            --turnsToSprout;
            if (energy > ENERGY_REQUIRED_TO_SPROUT
                    && turnsToSprout <= 0
                    && !(grid.getCell(cell, Direction.left).object instanceof TreePart)
                    && !(grid.getCell(cell, Direction.right).object instanceof TreePart)){
                energy -= ENERGY_REQUIRED_TO_SPROUT;
                sprout(world);
                return true;
            }
        }
        return false;
    }

    private void sprout(EvoTreeWorld world) {
        CellGrid grid = world.getGrid();
        Tree tree = Tree.getInstance();
        tree.generation = this.generation;
        tree.dna.set(this.dna);
        tree.init();
        tree.energy = this.energy;
        tree.root = this.cell;
        TreePart treePart = TreePart.getInstance();
        treePart.type = TreePartType.SHOOT;
        treePart.buffer.set(this.dna);
        grid.addCellObject(treePart, tree.root);
        treePart.activeGene = tree.dna.getGene(0);
        treePart.lightAbsorption = TreePart.calcLightAbsorption(treePart.activeGene.getValue(Gene.LIGHT_ABSORPTION));
        tree.addPart(treePart);
        tree.justSprouted = true;
        world.addTree(tree);
        log.info("Tree #" + tree.id + " was sprouted from seed #" + id + " with energy " + tree.energy);
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
        state = null;
        turnsToSprout = -1;
    }
}
