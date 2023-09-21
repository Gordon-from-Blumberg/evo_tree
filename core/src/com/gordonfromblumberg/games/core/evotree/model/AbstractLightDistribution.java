package com.gordonfromblumberg.games.core.evotree.model;

public abstract class AbstractLightDistribution implements LightDistribution {
    protected int turn = 1;
    protected int width;
    protected int height;
    protected float lightAbsorption;

    protected AbstractLightDistribution(int width, int height, float lightAbsorption) {
        this.width = width;
        this.height = height;
        this.lightAbsorption = lightAbsorption;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getLightAbsorption() {
        return lightAbsorption;
    }

    @Override
    public int nextTurn() {
        return ++turn;
    }
}
