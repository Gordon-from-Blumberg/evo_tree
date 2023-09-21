package com.gordonfromblumberg.games.core.evotree.model;

public interface LightDistribution {
    int getWidth();
    int getHeight();

    int getLight(int x, int y);
    float getLightAbsorption();

    int nextTurn();
}
