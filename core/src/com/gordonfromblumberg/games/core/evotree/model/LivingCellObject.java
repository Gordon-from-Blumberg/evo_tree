package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public abstract class LivingCellObject extends CellObject {
    protected static final int[] LIGHT_MODS = new int[] {1, 2, 4, 2};

    /**
     * Updates state of this tree part
     * @param world Game world
     * @return true if this part should be removed from world
     */
    public boolean update(EvoTreeWorld world) {
        return false;
    }

    public int calcLight(CellGrid grid) {
        int max = 0;
        for (Direction dir : Direction.ALL) {
            Cell next = grid.getCell(cell, dir);
            if (next != null && next.object == null) {
                int light = next.sunLight / LIGHT_MODS[dir.getCode()];
                if (light > max) {
                    max = light;
                }
            }
        }
        return Math.min(max, getLightAbsorption());
    }
}
