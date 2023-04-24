package com.gordonfromblumberg.games.core.common.chunk;

import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.utils.IntVector2;

public class ChunkManager<T> {
    private final Chunk<T>[][] chunks;
    private final int chunkSize;
    private final int gridWidth;
    private final int gridHeight;
    private final int chunkGridWidth;
    private final int chunkGridHeight;

    private final Array<T> objects = new Array<>();

    public ChunkManager(int gridWidth, int gridHeight, int chunkSize) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.chunkSize = chunkSize;
        this.chunkGridWidth = gridWidth % chunkSize > 0 ? gridWidth / chunkSize + 1 : gridWidth / chunkSize;
        this.chunkGridHeight = gridHeight % chunkSize > 0 ? gridHeight / chunkSize + 1 : gridHeight / chunkSize;

        this.chunks = new Chunk[chunkGridWidth][chunkGridHeight];
        for (int i = 0; i < chunkGridWidth; ++i) {
            for (int j = 0; j < chunkGridHeight; ++j) {
                this.chunks[i][j] = new Chunk<>(i, j, chunkSize, gridWidth, gridHeight);
            }
        }
    }

    public void addObject(T object, int x, int y) {
        chunks[x / chunkSize][y / chunkSize].addObject(object, x, y);
    }

    public void removeObject(T object, int x, int y) {
        chunks[x / chunkSize][y / chunkSize].removeObject(object);
    }

    public Array<T> findObjectsUnderLine(int x1, int y1, int x2, int y2) {
        final Array<T> result = this.objects;
        result.clear();
        while (x1 < 0 || x1 >= gridWidth) {
            x1 = (x1 + gridWidth) % gridWidth;
        }
        while (x2 < 0 || x2 >= gridWidth) {
            x2 = (x2 + gridWidth) % gridWidth;
        }

        final int chunkSize = this.chunkSize;
        final Chunk<T>[][] chunks = this.chunks;
        float k = x2 != x1 ? ((float) y2 - y1) / (x2 - x1) : 0;

        int minChunkX = x1 / chunkSize;
        int maxChunkX = x2 / chunkSize;
        int maxChunkY = (k > 0 ? y2 : y1) / chunkSize;

        final int chunkGridWidth = this.chunkGridWidth;
        for (int i = 0, to = minChunkX < maxChunkX ? maxChunkX - minChunkX + 1
                : minChunkX > maxChunkX ? maxChunkX + chunkGridWidth - minChunkX + 1
                : x1 < x2 ? 1 : chunkGridWidth; i < to; ++i) {
            int chunkX = (minChunkX + i) % chunkGridWidth;
            Chunk<T>[] chunkColumn = chunks[chunkX];
            for (int chunkY = maxChunkY; chunkY >= 0; --chunkY) {
                Chunk<T> chunk = chunkColumn[chunkY];
                IntVector2 chunkCorner = k > 0 ? chunk.bottomRight : chunk.bottomLeft;
                if (y1 + k * (chunkCorner.x - x1) >= chunkCorner.y) {
                    for (ChunkItem<T> item : chunk) {
                        if ((x1 <= x2 && item.x >= x1 && item.x <= x2
                                || x1 > x2 && (item.x >= x1 || item.x <= x2))
                                && y1 + k * (item.x - x1) >= item.y) {
                            result.add(item.object);
                        }
                    }
                }
            }
        }

        return result;
    }
}
