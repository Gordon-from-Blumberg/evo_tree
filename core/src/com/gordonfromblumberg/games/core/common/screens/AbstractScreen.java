package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gordonfromblumberg.games.core.common.Main;

public abstract class AbstractScreen implements Screen {

    private static final float MAX_DELTA = 1.0f / 30;

    protected AssetManager assets;

    protected SpriteBatch batch;
    protected Color color = Color.BLACK;

    protected Renderer worldRenderer;
    protected UIRenderer uiRenderer;

    protected boolean initialized;

    protected AbstractScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    protected void initialize() {
        Gdx.app.log("INIT", "AbstractScreen.initialize for " + getClass().getSimpleName());
        assets = Main.getInstance().assets();

        createWorldRenderer();
        createUiRenderer();
    }

    @Override
    public void show() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, MAX_DELTA);

        Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        if (worldRenderer != null)
            worldRenderer.render(delta);

        uiRenderer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        if (worldRenderer != null)
            worldRenderer.resize(width, height);
        uiRenderer.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    protected void update(float delta) {
    }

    /**
     * Implementation of AbstractScreen does not create world renderer.
     * This method should be overridden.
     */
    protected void createWorldRenderer() {
        Gdx.app.log("INIT", "AbstractScreen.createWorldRenderer for " + getClass().getSimpleName());
    }

    protected void createUiRenderer() {
        Gdx.app.log("INIT", "AbstractScreen.createUiRenderer for " + getClass().getSimpleName());

        uiRenderer = new UIRenderer(batch);
    }

    @Override
    public void dispose() {
        if (worldRenderer != null)
            worldRenderer.dispose();
        uiRenderer.dispose();
    }
}
