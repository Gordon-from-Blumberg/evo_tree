package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.ObjectMap;

public abstract class AbstractLightDistributionDecorator implements LightDistribution {
    protected LightDistribution original;

    protected AbstractLightDistributionDecorator(LightDistribution original) {
        this.original = original;
    }

    @Override
    public int getWidth() {
        return original.getWidth();
    }

    @Override
    public int getHeight() {
        return original.getHeight();
    }

    @Override
    public int nextTurn() {
        return original.nextTurn();
    }

    @FunctionalInterface
    public interface LightDistributionDecoratorProducer {
        AbstractLightDistributionDecorator create(LightDistribution original, ObjectMap<String, Object> params);
    }
}
