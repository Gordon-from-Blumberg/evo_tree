package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.math.MathUtils;

public class ChangeLightByX extends AbstractLightDistributionDecorator {
    public static final LightDistributionDecoratorProducer PRODUCER = (original, params) -> new ChangeLightByX(
            original,
            (int) params.get("ChangeLightByX.halfMagnitude")
    );

    private final int[] shifts;

    private ChangeLightByX(LightDistribution original, int halfMagnitude) {
        super(original);

        shifts = new int[original.getWidth()];
        for (int i = 0, n = shifts.length; i < n; ++i) {
            shifts[i] = MathUtils.round(MathUtils.sin(MathUtils.PI2 * (i + 0.5f) / n) * halfMagnitude);
        }
    }

    @Override
    public int getLight(int x, int y) {
        return original.getLight(x, y) + shifts[x];
    }
}
