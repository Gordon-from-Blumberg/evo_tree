package com.gordonfromblumberg.games.core.common.world;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public class GameWorldParams {
    int width = 250;
    int height = 50;
    int cellSize = 20;
    int sunLight = 60;

    GameWorldParams() {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        if (configManager.contains("world.width")) width = configManager.getInteger("world.width");
        if (configManager.contains("world.height")) height = configManager.getInteger("world.height");
        if (configManager.contains("world.cellSize")) cellSize = configManager.getInteger("world.cellSize");
        if (configManager.contains("world.sunLight")) sunLight = configManager.getInteger("world.sunLight");
    }
}
