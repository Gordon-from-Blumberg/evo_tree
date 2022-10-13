package com.gordonfromblumberg.games.core.evotree.model;

public class ChangeLightByTime extends AbstractLightDistributionDecorator {
    private int max;
    private int min;
    private int delay;
    private int step;

    private int shift;

    public ChangeLightByTime(LightDistribution original, int max, int min, int delay, int step) {
        super(original);

        this.max = max;
        this.min = min;
        this.delay = delay;
        this.step = step;
    }

    @Override
    public int getLight(int x, int y) {
        return original.getLight(x, y) + shift;
    }

    @Override
    public void nextTurn() {
        super.nextTurn();

        if (turn % delay == 0) {
            shift += step;
            if (shift == max || shift == min)
                step = -step;
        }
    }
}
