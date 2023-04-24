package com.gordonfromblumberg.games.core.common.chunk;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.IntVector2;

import java.util.Iterator;

public class Chunk<T> implements Iterable<ChunkItem<T>> {
    private final Pool<ChunkItem<T>> chunkItemPool = new Pool<ChunkItem<T>>() {
        @Override
        protected ChunkItem<T> newObject() {
            return new ChunkItem<>();
        }
    };

    final Array<ChunkItem<T>> objects = new Array<>();
    final IntVector2 bottomLeft = new IntVector2();
    final IntVector2 bottomRight = new IntVector2();
    final IntVector2 topLeft = new IntVector2();
    final IntVector2 topRight = new IntVector2();

    Chunk(int x, int y, int size, int gridWidth, int gridHeight) {
        int right = x * size + size - 1;
        if (right >= gridWidth)
            right = gridWidth - 1;
        int top = y * size + size - 1;
        if (top >= gridHeight)
            top = gridHeight - 1;

        bottomLeft.set(x * size, y * size);
        bottomRight.set(right, y * size);
        topLeft.set(x * size, top);
        topRight.set(right, top);
    }

    void addObject(T object, int x, int y) {
        objects.add(chunkItemPool.obtain().set(x, y, object));
    }

    // O(n) of objects inside chunk
    void removeObject(T object) {
        Iterator<ChunkItem<T>> it = objects.iterator();
        while (it.hasNext()) {
            if (it.next().object == object) {
                it.remove();
                break;
            }
        }
    }

    @Override
    public Iterator<ChunkItem<T>> iterator() {
        return objects.iterator();
    }
}
