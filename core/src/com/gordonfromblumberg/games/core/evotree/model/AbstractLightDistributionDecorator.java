package com.gordonfromblumberg.games.core.evotree.model;

public abstract class AbstractLightDistributionDecorator extends AbstractLightDistribution {
    protected LightDistribution original;

    protected AbstractLightDistributionDecorator(LightDistribution original) {
        super(original.getWidth(), original.getHeight());

        this.original = original;
    }
}
