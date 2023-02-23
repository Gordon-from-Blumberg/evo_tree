package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.gordonfromblumberg.games.core.evotree.model.AbstractLightDistributionDecorator;
import com.gordonfromblumberg.games.core.evotree.model.LightDistribution;

public class GameWorldParams {
    int width = 250;
    int height = 50;
    int sunLight = 60;
    int lightAbsorptionStep = 1;
    final Queue<AbstractLightDistributionDecorator.LightDistributionDecoratorProducer> decoratorProducers = new Queue<>(4);
    final ObjectMap<String, Object> decoratorParams = new ObjectMap<>(8);

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSunLight() {
        return sunLight;
    }

    public void setSunLight(int sunLight) {
        this.sunLight = sunLight;
    }

    public int getLightAbsorptionStep() {
        return lightAbsorptionStep;
    }

    public void setLightAbsorptionStep(int lightAbsorptionStep) {
        this.lightAbsorptionStep = lightAbsorptionStep;
    }

    public void setDecoratorParam(String name, Object value) {
        decoratorParams.put(name, value);
    }

    public void addDecorator(AbstractLightDistributionDecorator.LightDistributionDecoratorProducer decoratorProducer) {
        decoratorProducers.addLast(decoratorProducer);
    }

    public void removeDecorator(AbstractLightDistributionDecorator.LightDistributionDecoratorProducer decoratorProducer) {
        decoratorProducers.removeValue(decoratorProducer, true);
    }

    public LightDistribution decorate(LightDistribution original) {
        LightDistribution result = original;
        while (decoratorProducers.notEmpty()) {
            result = decoratorProducers.removeFirst().create(result, decoratorParams);
        }
        return result;
    }
}
