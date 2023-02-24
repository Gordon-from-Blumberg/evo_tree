package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evotree.model.AbstractLightDistributionDecorator;
import com.gordonfromblumberg.games.core.evotree.model.LightDistribution;

import java.util.Map;

public class GameWorldParams {
    int width = 250;
    int height = 50;
    int sunLight = 60;
    int lightAbsorptionStep = 1;
    final Queue<String> selectedDecorators = new Queue<>(4);
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

    public void addDecorator(String decoratorName) {
        selectedDecorators.addLast(decoratorName);
    }

    public void removeDecorator(String decoratorName) {
        selectedDecorators.removeValue(decoratorName, false);
    }

    public LightDistribution decorate(LightDistribution original) {
        LightDistribution result = original;
        while (selectedDecorators.notEmpty()) {
            result = AbstractLightDistributionDecorator.decorate(selectedDecorators.removeFirst(), result, decoratorParams);
        }
        return result;
    }

    public boolean isSelected(String decoratorName) {
        for (String selected : selectedDecorators) {
            if (selected.equals(decoratorName)) {
                return true;
            }
        }
        return false;
    }

    public void loadFromConfig(ConfigManager config) {
        width = config.getInteger("world.width");
        height = config.getInteger("world.height");
        sunLight = config.getInteger("world.sunLight");
        lightAbsorptionStep = config.getInteger("world.lightAbsorptionStep");
        selectedDecorators.clear();
    }

    public void loadFromPreferences(Preferences prefs) {
        width = prefs.getInteger("world.width");
        height = prefs.getInteger("world.height");
        sunLight = prefs.getInteger("world.sunLight");
        lightAbsorptionStep = prefs.getInteger("world.lightAbsorptionStep");

        selectedDecorators.clear();
        String decorators = prefs.getString("selectedDecorators");
        if (!decorators.isEmpty()) {
            String[] decoratorNames = decorators.split(",");
            for (String decoratorName : decoratorNames) {
                selectedDecorators.addLast(decoratorName);
            }
        }

        String keyPrefix = "decorator.param.";
        for (Map.Entry<String, ?> entry : prefs.get().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(keyPrefix)) {
                decoratorParams.put(key.substring(keyPrefix.length()), entry.getValue());
            }
        }
    }

    public void saveToPreferences(Preferences prefs) {
        prefs.putInteger("world.width", width);
        prefs.putInteger("world.height", height);
        prefs.putInteger("world.sunLight", sunLight);
        prefs.putInteger("world.lightAbsorptionStep", lightAbsorptionStep);
        prefs.putString("selectedDecorators", selectedDecorators.toString(","));
        for (ObjectMap.Entry<String, Object> entry : decoratorParams) {
            String key = "decorator.param." + entry.key;
            if (entry.value instanceof Boolean) prefs.putBoolean(key, (Boolean) entry.value);
            else if (entry.value instanceof Integer) prefs.putInteger(key, (Integer) entry.value);
            else if (entry.value instanceof Long) prefs.putLong(key, (Long) entry.value);
            else if (entry.value instanceof String) prefs.putString(key, (String) entry.value);
            else if (entry.value instanceof Float) prefs.putFloat(key, (Float) entry.value);
            else throw new IllegalStateException("Unexpected decorator parameter: " + entry);
        }
    }
}
