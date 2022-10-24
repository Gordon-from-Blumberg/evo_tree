package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface Renderer extends Disposable {
    void render(float dt);
    void resize(int width, int height);
    OrthographicCamera getCamera();
    Viewport getViewport();

    void screenToViewport(float x, float y, Vector3 out);
    void viewportToScreen(float x, float y, Vector3 out);
}
