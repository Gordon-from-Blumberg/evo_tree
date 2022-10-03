package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class Shoot extends Wood {
    private static final Pool<Shoot> pool = new Pool<Shoot>() {
        @Override
        protected Shoot newObject() {
            return new Shoot();
        }
    };

    Gene activeGene;

    private Shoot() {}

    public static Shoot getInstance() {
        return pool.obtain();
    }

    void sprout(EvoTreeWorld world) {
        Cell cell = this.cell;
        Wood wood = Wood.getInstance();
        wood.id = world.nextPartId();
        wood.setCell(cell);
        tree.addWood(wood);

        CellGrid grid = world.getGrid();
        for (Direction dir : Direction.ALL) {
            int nextActiveGene = activeGene.getValue(dir);
            if (nextActiveGene < 16) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.treePart == null) {
                    Shoot shoot = getInstance();
                    shoot.id = world.nextPartId();
                    shoot.setCell(neib);
                    tree.addShoot(shoot);
                    shoot.activeGene = tree.dna.genes[nextActiveGene];
                }
            }
        }
    }

    int canMakeChildrenCount(CellGrid grid) {
        int result = 0;
        for (Direction dir : Direction.ALL) {
            if (activeGene.getValue(dir) < 16) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.treePart == null) {
                    ++result;
                }
            }
        }
        return result;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        activeGene = null;
    }
}
