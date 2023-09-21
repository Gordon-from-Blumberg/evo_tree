package com.gordonfromblumberg.games.core.evotree.model;

public class Cell {
    int x, y;
    int sunLight;
    boolean underSun;
    CellObject object;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void updateSunLight(int light) {
        sunLight = light < 0 ? 0 : light;
    }

    public int getSunLight() {
        return object != null ? sunLight - object.getLightAbsorption() : sunLight;
    }

    public boolean isUnderSun() {
        return underSun;
    }

    public CellObject getObject() {
        return object;
    }

    public void setObject(CellObject object) {
        this.object = object;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
