package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
public interface CoordsConverter {
    void convert(float x, float y, Vector3 out);
}
