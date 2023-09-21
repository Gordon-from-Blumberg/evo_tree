package com.gordonfromblumberg.games.core.evotree.model;

public class LightSource extends CellObject {
    int light;

    public LightSource(int light) {
        this.lightAbsorption = 1_000_000;
        this.light = light;
    }
}
