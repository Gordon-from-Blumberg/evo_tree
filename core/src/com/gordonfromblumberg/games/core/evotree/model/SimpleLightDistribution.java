package com.gordonfromblumberg.games.core.evotree.model;

public class SimpleLightDistribution extends AbstractLightDistribution {
    private int sunLight;
    private int step = 1;

    public SimpleLightDistribution(int width, int height, int sunLight, int step) {
        super(width, height);

        this.sunLight = sunLight;
        if (step < 1) {
            throw new IllegalArgumentException("Step should be at least 1, but passed " + step);
        }
        this.step = step;
    }

    @Override
    public int getLight(int x, int y) {
        return sunLight - (height - y - 1) / step;
    }
}
