package com.gordonfromblumberg.games.core.common.world;

public class GameWorldParams {
    int width = 250;
    int height = 50;
    int sunLight = 60;
    int lightAbsorptionStep = 1;

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
}
