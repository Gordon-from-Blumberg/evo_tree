package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
public interface CoordsConverter {
    void convert(int x, int y, Vector3 out);
}
