package com.gordonfromblumberg.games.core.common.utils;

@FunctionalInterface
public interface ClickHandler {
    void onClick(int button, float x, float y);
}
