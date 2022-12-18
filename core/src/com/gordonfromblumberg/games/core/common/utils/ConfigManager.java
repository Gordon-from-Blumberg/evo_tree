package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;
import com.gordonfromblumberg.games.core.common.Main;

import java.io.BufferedReader;
import java.io.IOException;

public class ConfigManager {
    private static final String DEFAULT_CONFIG_FILE = "config/default-config.properties";
    private static final String CONFIG_FILE = "config/config.properties";
    private static final String CONFIG_PREFERENCE = Main.NAME + ".config";

    protected final ObjectMap<String, String> configProperties = new ObjectMap<>();

    public ConfigManager() {
    }

    public void init() {
        loadConfig(DEFAULT_CONFIG_FILE);
        loadConfig(CONFIG_FILE);
    }

    public String getString(String property) {
        return configProperties.get(property);
    }

    public void setString(String propertyName, String value) {
        configProperties.put(propertyName, value);
    }

    public boolean getBoolean(String propertyName) {
        String property = configProperties.get(propertyName);
        return Boolean.parseBoolean(property);
    }

    public void setBoolean(String propertyName, boolean value) {
        configProperties.put(propertyName, String.valueOf(value));
    }

    public int getInteger(String propertyName) {
        String property = configProperties.get(propertyName);
        if (property != null) {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        StringUtils.format("Couldn't parse property # = #", propertyName, property),
                        e
                );
            }
        }
        return 0;
    }

    public int getInteger(String propertyName, boolean usePreference) {
        if (usePreference) {
            Preferences prefs = Gdx.app.getPreferences(CONFIG_PREFERENCE);
            int value = prefs.getInteger(propertyName);
            if (value != 0)
                return value;
        }

        return getInteger(propertyName);
    }

    public float getFloat(String propertyName) {
        String property = configProperties.get(propertyName);
        if (property != null) {
            try {
                return Float.parseFloat(property);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        StringUtils.format("Couldn't parse property # = #", propertyName, property),
                        e
                );
            }
        }
        return 0f;
    }

    public long getLong(String propertyName) {
        String property = configProperties.get(propertyName);
        if (property != null) {
            try {
                return Long.parseLong(property);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        StringUtils.format("Couldn't parse property # = #", propertyName, property),
                        e
                );
            }
        }
        return 0L;
    }

    public void getColor(String propertyName, Color out) {
        String property = configProperties.get(propertyName);
        if (property != null) {
            String[] rgba = property.split(",");
            if (rgba.length >= 3) {
                out.r = Float.parseFloat(rgba[0].trim());
                out.g = Float.parseFloat(rgba[1].trim());
                out.b = Float.parseFloat(rgba[2].trim());
                if (rgba.length == 4) {
                    out.a = Float.parseFloat(rgba[3].trim());
                } else {
                    out.a = 1f;
                }
            }
        }
    }

    public boolean contains(String propertyName) {
        String property = configProperties.get(propertyName);
        return property != null && !property.trim().isEmpty();
    }

    protected void loadConfig(String configPath) {
        try (BufferedReader reader = new BufferedReader(Gdx.files.internal(configPath).reader())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty())
                    continue;

                String[] keyAndValue = line.split("=");
                configProperties.put(keyAndValue[0].trim(), keyAndValue[1].trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load config from " + configPath, e);
        }
    }
}
