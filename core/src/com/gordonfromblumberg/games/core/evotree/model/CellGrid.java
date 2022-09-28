package com.gordonfromblumberg.games.core.evotree.model;

public class CellGrid {
    int width, height;
    public final Cell[][] cells;

    public CellGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public void updateSunLight(int sunLight) {
        final Cell[][] cells = this.cells;
        int light = sunLight;
        for (int h = height, j = h - 1; j >= 0; --j) {
            if ((h - j) % 3 == 0) {
                --light;
            }
            boolean top = j == h - 1;
            for (int i = 0, w = width; i < w; ++i) {
                Cell cell = cells[i][j];
                TreePart treePart = cell.treePart;
                if (top) {
                    cell.underSun = true;
                    cell.updateSunLight(light);
                } else {
                    Cell upperCell = cells[i][j + 1];
                    cell.underSun = upperCell.underSun && upperCell.treePart == null;
                    // todo find the closest and brightest light source if not under sun
                    cell.updateSunLight(light);
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
