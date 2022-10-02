package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.utils.Poolable;

public abstract class TreePart implements Poolable {
    protected int id;
    protected Cell cell;

    public void update() {}

    public void setId(int id) {
        this.id = id;
    }

    public abstract int getLightAbsorption();
}
