package com.gordonfromblumberg.games.core.evotree.model;

public class CellGrid {
    private static final int[][] NEIGHBORS = new int[][] {
            {0, 1},
            {1, 0},
            {0, -1},
            {-1, 0}
    };

    int width, height;
    int cellSize;
    public final Cell[][] cells;
    private final int[] treeHeights;

    public CellGrid(int width, int height, int cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.cells = new Cell[width][height];
        this.treeHeights = new int[width];

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public void updateSunLight(LightDistribution lightDistribution) {
        final Cell[][] cells = this.cells;
        final int[] treeHeights = this.treeHeights;
        // go from top to bottom and calculate sunlight for each cell
        // and write to treeHeights the first cell is not under sun
        for (int i = 0, w = width; i < w; ++i) {
            Cell[] col = cells[i];
            boolean treeHeightUpdated = false;
            for (int h = height, j = h - 1; j >= 0; --j) {
                Cell cell = col[j];
                int light = lightDistribution.getLight(i, j);

                if (j == h - 1) {
                    cell.underSun = true;
                    cell.updateSunLight(light);
                } else {
                    Cell upperCell = cells[i][j + 1];
                    cell.underSun = upperCell.underSun && upperCell.treePart == null;
                    cell.updateSunLight(cell.underSun ? light : upperCell.sunLight - 2);

                    if (!cell.underSun && !treeHeightUpdated) {
                        treeHeights[i] = j;
                        treeHeightUpdated = true;
                    }
                }
            }
            if (!treeHeightUpdated) {
                treeHeights[i] = -1;
            }
        }

        for (int i = 0, w = width; i < w; ++i) {
            int l = i - 1;
            if (l < 0) l = w - 1;
            int r = i + 1;
            if (r == w) r = 0;

            int treeHeight = treeHeights[l];
            for (; treeHeight >= 0; --treeHeight) {
                calcLight(cells[l][treeHeight], cells[i][treeHeight].sunLight - 2, Direction.left);
            }
            treeHeight = treeHeights[r];
            for (; treeHeight >= 0; --treeHeight) {
                calcLight(cells[r][treeHeight], cells[i][treeHeight].sunLight - 2, Direction.right);
            }
        }
    }

    private void calcLight(Cell cell, int light, Direction dir) {
        int old = cell.getSunLight();
        if (light > old) {
            if (cell.updateSunLight(light) > old) {
                for (Direction d : Direction.ALL) {
                    if (d != dir.opposite()) {
                        Cell next = getCell(cell, d);
                        if (next != null && !next.underSun) {
                            calcLight(next, cell.getSunLight() - 2, d);
                        }
                    }
                }
            }
        }
    }

    public Cell findCell(int x, int y) {
        int cellX = x / cellSize;
        if (cellX < 0 || cellX >= width) {
            return null;
        }
        int cellY = y / cellSize;
        if (cellY < 0 || cellY >= height) {
            return null;
        }
        return cells[cellX][cellY];
    }

    Cell getCell(Cell cell, Direction dir) {
        int[] dc = NEIGHBORS[dir.getCode()];
        int y = cell.y + dc[1];
        if (y < 0 || y >= height) {
            return null;
        }
        int x = cell.x + dc[0];
        if (x < 0) x = width - 1;
        if (x == width) x = 0;
        return cells[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCellSize() {
        return cellSize;
    }
}
