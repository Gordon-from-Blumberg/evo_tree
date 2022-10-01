package com.gordonfromblumberg.games.core.evotree.model;

public class LightingTest extends TreePart {
    private int lightAbsorption;

    public LightingTest(int lightAbsorption) {
        this.lightAbsorption = lightAbsorption;
    }

    @Override
    public int getLightAbsorption() {
        return lightAbsorption;
    }

    public void setLightAbsorption(int lightAbsorption) {
        this.lightAbsorption = lightAbsorption;
    }
}
