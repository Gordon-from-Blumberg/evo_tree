package com.gordonfromblumberg.games.core.evotree.model;

public class CellGrid {
    private static final int[][] NEIGHBORS = new int[][] {
            {0, 1},
            {1, 0},
            {0, -1},
            {-1, 0}
    };

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

    public void updateSunLight(int turn, int sunLight) {
        final Cell[][] cells = this.cells;
        int light = sunLight;
        for (int h = height, j = h - 1; j >= 0; --j) {
            if ((h - j) % 3 == 0) {
                --light;
            }
            boolean top = j == h - 1;
            for (int i = 0, w = width; i < w; ++i) {
                Cell cell = cells[i][j];
                if (cell.lastTurnUpdated == turn) {
                    continue;
                }

                TreePart treePart = cell.treePart;
                if (top) {
                    cell.underSun = true;
                    cell.updateSunLight(turn, light);
                } else {
                    Cell upperCell = cells[i][j + 1];
                    cell.underSun = upperCell.underSun && upperCell.treePart == null;
                    if (cell.underSun) {
                        cell.updateSunLight(turn, light);
                    } else {
                        calcLight(cell, turn);
                    }
                }
            }
        }
    }

    private int calcLight(Cell cell, int turn) {
        if (cell.lastTurnUpdated == turn) {
            return cell.sunLight;
        }

        cell.calcLightInProcess = true;
        int maxLight = 0;
        for (int i = 0; i < 4; ++i) {
            int[] dCoords = NEIGHBORS[i];
            int x = cell.x + dCoords[0];
            if (x < 0 || x >= width) {
                continue;
            }
            int y = cell.y + dCoords[1];
            if (y < 0 || y >= height) {
                continue;
            }

            Cell neib = cells[x][y];
            if (neib.calcLightInProcess) {
                continue;
            }

            int light = calcLight(neib, turn);
            if (light > maxLight) {
                maxLight = light;
            }
        }

        cell.calcLightInProcess = false;
        cell.updateSunLight(turn, maxLight - 2);
        return cell.sunLight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
