package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class Seed extends TreePart {
    private static final Pool<Seed> pool = new Pool<Seed>() {
        @Override
        protected Seed newObject() {
            return new Seed();
        }
    };

    private static final int LIGHT_REQUIRED_TO_SPROUT = 10;
    private static final int ENERGY_REQUIRED_TO_SPROUT = 10;

    int generation;
    final DNA dna = new DNA();
    int energy;

    private Seed() {
        super(5);
    }

    public static Seed getInstance() {
        return pool.obtain();
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
            for (int i = 0; i < 2; ++i) {
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

        if (energy > ENERGY_REQUIRED_TO_SPROUT && calcLight(grid) >= LIGHT_REQUIRED_TO_SPROUT) {
            energy -= ENERGY_REQUIRED_TO_SPROUT;
            sprout(world);
            return true;
        }
        return false;
    }

    private void sprout(EvoTreeWorld world) {
        Tree tree = Tree.getInstance();
        tree.generation = this.generation;
        tree.turnsRemain = Gene.RAND.nextInt(64, 128);
        tree.dna.set(this.dna);
        tree.energy = this.energy;
        Shoot shoot = Shoot.getInstance();
        shoot.id = world.nextPartId();
        shoot.setCell(this.cell);
        shoot.activeGene = tree.dna.genes[0];
        tree.addShoot(shoot);
        tree.justSprouted = true;
        world.addTree(tree);
    }

    @Override
    public int getLightAbsorption() {
        return 100;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        generation = 0;
        dna.reset();
        energy = 0;
    }
}
