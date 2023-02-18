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

    int updateSunLight(int light) {
        CellObject tp = object;
        if (tp != null) {
            light -= tp.getLightAbsorption();
        }
        sunLight = light;
        if (sunLight < 0) {
            sunLight = 0;
        }
//        if (y < 10) sunLight >>= 1;
        return sunLight;
    }

    public int getSunLight() {
        return sunLight;
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
