package com.gordonfromblumberg.games.core.common.factory;

import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public abstract class AbstractFactory {
    protected static AbstractFactory instance;

    private ConfigManager configManager;

    public static AbstractFactory getInstance() {
        return instance;
    }

    public ConfigManager configManager() {
        if (configManager == null)
            configManager = createConfigManager();

        return configManager;
    }

    protected ConfigManager createConfigManager() {
        return new ConfigManager();
    }
}
