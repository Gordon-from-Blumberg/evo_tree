package com.gordonfromblumberg.games.core.evotree.model;

public class SimpleLightDistribution extends AbstractLightDistribution {
    private int sunLight;

    public SimpleLightDistribution(int width, int height, int sunLight, float lightAbsorption) {
        super(width, height, lightAbsorption);

        this.sunLight = sunLight;
    }

    @Override
    public int getLight(int x, int y) {
        return sunLight - (height - y - 1);
    }
}
