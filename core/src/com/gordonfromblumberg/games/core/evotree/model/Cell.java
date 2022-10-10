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

    int updateSunLight(int light) {
        TreePart tp = treePart;
        if (tp != null) {
            light -= tp.getLightAbsorption();
        }
        sunLight = light;
        if (sunLight < 0) {
            sunLight = 0;
        }
        if (y < 10) sunLight >>= 1;
        return sunLight;
    }

    public int getSunLight() {
        return sunLight;
    }

    public boolean isUnderSun() {
        return underSun;
    }

    public TreePart getTreePart() {
        return treePart;
    }

    public void setTreePart(TreePart treePart) {
        this.treePart = treePart;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
