package com.gordonfromblumberg.games.core.evotree.model;

public class Cell {
    int x, y;
    int sunLight;
    boolean underSun;
    TreePart treePart;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void updateSunLight(int light) {
        TreePart tp = treePart;
        if (tp == null) {
            sunLight = light;
        } else {
            light -= tp.getLightAbsorption();
            sunLight = light < 0 ? 0 : light;
        }
    }

    public int getSunLight() {
        return sunLight;
    }
}
