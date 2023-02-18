package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class CellObject implements Poolable {
    private static final Pool<CellObject> pool = new Pool<CellObject>() {
        @Override
        protected CellObject newObject() {
            return new CellObject();
        }
    };

    protected Cell cell;
    protected int lightAbsorption;

    protected CellObject() {}

    public void setCell(Cell cell) {
        this.cell = cell;
        cell.object = this;
    }

    public int getLightAbsorption() {
        return lightAbsorption;
    }

    @Override
    public void reset() {
        if (cell != null) {
            if (cell.object == this) {
                cell.object = null;
            }
            cell = null;
        }
        lightAbsorption = 0;
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
