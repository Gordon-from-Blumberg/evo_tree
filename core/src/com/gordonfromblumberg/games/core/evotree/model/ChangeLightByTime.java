package com.gordonfromblumberg.games.core.evotree.model;

public class ChangeLightByTime extends AbstractLightDistributionDecorator {
    public static final LightDistributionDecoratorProducer PRODUCER = (original, params) -> new ChangeLightByTime(
            original,
            (int) params.get("ChangeLightByTime.max"),
            (int) params.get("ChangeLightByTime.min"),
            (int) params.get("ChangeLightByTime.delay"),
            (int) params.get("ChangeLightByTime.step")
    );

    private int max;
    private int min;
    private int delay;
    private int step;

    private int shift;

    private ChangeLightByTime(LightDistribution original, int max, int min, int delay, int step) {
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
    public int nextTurn() {
        int nextTurn = super.nextTurn();

        if (nextTurn % delay == 0) {
            shift += step;
            if (shift >= max && step > 0 || shift <= min && step < 0)
                step = -step;
        }
        return nextTurn;
    }
}
