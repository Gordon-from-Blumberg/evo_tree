package com.gordonfromblumberg.games.core.evotree.model;

public class LightingTest extends CellObject {
    public LightingTest(int lightAbsorption) {
        this.lightAbsorption = lightAbsorption;
    }

    public void setLightAbsorption(int lightAbsorption) {
        this.lightAbsorption = lightAbsorption;
    }

    @Override
    public void release() {}

    @Override
    public void reset() {}
}
