package com.gordonfromblumberg.games.core.evotree.model;

public class LightingTest extends TreePart {
    private int lightAbsorption;

    public LightingTest(int lightAbsorption) {
        super(0);
        this.lightAbsorption = lightAbsorption;
    }

    @Override
    public int getLightAbsorption() {
        return lightAbsorption;
    }

    public void setLightAbsorption(int lightAbsorption) {
        this.lightAbsorption = lightAbsorption;
    }

    @Override
    public void release() {}

    @Override
    public void reset() {}
}
