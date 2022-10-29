package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class UIViewport extends ExtendViewport {
    private final float minWorldWidth;
    private final float minWorldHeight;

    public UIViewport(float minWorldWidth, float minWorldHeight, Camera camera) {
        super(minWorldWidth, minWorldHeight, camera);

        this.minWorldWidth = minWorldWidth;
        this.minWorldHeight = minWorldHeight;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        setMinWorldWidth(screenWidth > minWorldWidth ? screenWidth : minWorldWidth);
        setMinWorldHeight(screenHeight > minWorldHeight ? screenHeight : minWorldHeight);

        super.update(screenWidth, screenHeight, centerCamera);
    }
}
