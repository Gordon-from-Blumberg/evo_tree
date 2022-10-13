package com.gordonfromblumberg.games.core.evotree.model;

public abstract class AbstractLightDistribution implements LightDistribution {
    protected int turn = 1;
    protected int width;
    protected int height;

    protected AbstractLightDistribution(int width, int height) {
        this.width = width;
        this.height = height;
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
    public void nextTurn() {
        ++turn;
    }
}
