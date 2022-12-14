package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.math.MathUtils;

public class ChangeLightByX extends AbstractLightDistributionDecorator {
    private int[] shifts = new int[width];
    private int halfMagnitude;
    private int periods = 1;

    public ChangeLightByX(LightDistribution original, int halfMagnitude) {
        super(original);

        for (int i = 0, n = width; i < n; ++i) {
            shifts[i] = MathUtils.round(MathUtils.sin(MathUtils.PI2 * (i + 0.5f) / n) * halfMagnitude);
        }
    }

    @Override
    public int getLight(int x, int y) {
        return original.getLight(x, y) + shifts[x];
    }
}
