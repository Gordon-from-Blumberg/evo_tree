package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public abstract class TreePart implements Poolable {
    protected static final int[] LIGHT_MODS = new int[] {1, 2, 4, 2};

    protected int id;
    protected Cell cell;
    protected int energyConsumption;

    public TreePart() {}

    protected TreePart(int energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    /**
     * Updates state of this tree part
     * @param world Game world
     * @return true if this part should be removed from world
     */
    public boolean update(EvoTreeWorld world) {
        return false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
        cell.treePart = this;
    }

    public int calcLight(CellGrid grid) {
        int max = 0;
        for (Direction dir : Direction.ALL) {
            Cell next = grid.getCell(cell, dir);
            if (next != null && next.treePart == null) {
                int light = next.sunLight / LIGHT_MODS[dir.getCode()];
                if (light > max) {
                    max = light;
                }
            }
        }
        return max;
    }

    public abstract int getLightAbsorption();

    @Override
    public void reset() {
        id = 0;
        if (cell != null) {
            if (cell.treePart == this) {
                cell.treePart = null;
            }
            cell = null;
        }
    }
}
