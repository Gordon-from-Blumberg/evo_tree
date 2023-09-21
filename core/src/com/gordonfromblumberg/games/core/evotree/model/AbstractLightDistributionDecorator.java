package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.ObjectMap;

public abstract class AbstractLightDistributionDecorator implements LightDistribution {
    protected static final ObjectMap<String, LightDistributionDecoratorProducer> decorators = new ObjectMap<>(4);

    protected LightDistribution original;

    protected AbstractLightDistributionDecorator(LightDistribution original) {
        this.original = original;
    }

    public static LightDistribution decorate(String decoratorName, LightDistribution original, ObjectMap<String, Object> params) {
        return decorators.get(decoratorName).create(original, params);
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
    public float getLightAbsorption() {
        return original.getLightAbsorption();
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
