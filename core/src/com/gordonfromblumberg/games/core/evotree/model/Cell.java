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
        if (tp == null) {
            sunLight = light;
        } else {
            light -= tp.getLightAbsorption();
            sunLight = light;
        }
        if (sunLight < 0) {
            sunLight = 0;
        }
        return sunLight;
    }

    public int getSunLight() {
        return sunLight;
    }

    public TreePart getTreePart() {
        return treePart;
    }

    public void setTreePart(TreePart treePart) {
        this.treePart = treePart;
    }
}
