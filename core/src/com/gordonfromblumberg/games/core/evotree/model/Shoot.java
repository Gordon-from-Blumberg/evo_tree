package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
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

    void sprout(EvoTreeWorld world, Array<Shoot> newShoots) {
        Cell cell = this.cell;
        Wood wood = Wood.getInstance();
        wood.setCell(cell);
        tree.addWood(wood);

        CellGrid grid = world.getGrid();
        for (Direction dir : Direction.ALL) {
            int nextActiveGene = activeGene.getValue(dir);
            if (nextActiveGene < 16) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.treePart == null) {
                    Shoot shoot = getInstance();
                    shoot.setCell(neib);
                    newShoots.add(shoot);
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

    boolean isBlocked(CellGrid grid) {
        for (Direction dir : Direction.ALL) {
            if (activeGene.getValue(dir) < 16) {
                Cell neib = grid.getCell(cell, dir);
                if (neib == null && dir == Direction.up
                        || neib != null && neib.treePart != null
                        && !(neib.treePart instanceof Wood && ((Wood) neib.treePart).tree == this.tree)) {
                    return true;
                }
            }
        }
        return false;
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
