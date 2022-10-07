package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public abstract class AbstractScreen implements Screen {

    private static final float MAX_DELTA = 1.0f / 30;

    protected AssetManager assets;

    protected SpriteBatch batch;
    protected Color color = Color.BLACK;

    protected Stage stage;
    // todo delete viewport from screen
    protected Viewport viewport;
    protected OrthographicCamera camera;
    protected Renderer worldRenderer, uiRenderer;

    protected Table uiRootTable;

    protected boolean initialized;

    protected AbstractScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    protected AbstractScreen(Renderer worldRenderer, Renderer uiRenderer) {
        this.worldRenderer = worldRenderer;
        this.uiRenderer = uiRenderer;
    }

    protected void initialize() {
        Gdx.app.log("INIT", "AbstractScreen.initialize for " + getClass().getSimpleName());
        assets = Main.getInstance().assets();

        createWorldViewport();
        createUiRenderer();

        createUI();
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

//        batch.begin();
        if (worldRenderer != null)
            worldRenderer.render(delta);
//        renderWorld(delta);
//        batch.end();

//        renderUi();
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
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    protected void createWorldViewport() {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final float worldWidth = configManager.getFloat("worldWidth");
        final float minRatio = configManager.getFloat("minRatio");
        final float maxRatio = configManager.getFloat("maxRatio");
        final float minWorldHeight = worldWidth / maxRatio;
        final float maxWorldHeight = worldWidth / minRatio;
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new ExtendViewport(worldWidth, minWorldHeight, worldWidth, maxWorldHeight, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    protected void createUiRenderer() {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final float worldWidth = configManager.getFloat("worldWidth");
        final float minRatio = configManager.getFloat("minRatio");
        final float maxRatio = configManager.getFloat("maxRatio");
        final float minWorldHeight = worldWidth / maxRatio;
        final float maxWorldHeight = worldWidth / minRatio;
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false);
        Viewport viewport = new ExtendViewport(worldWidth, minWorldHeight, worldWidth, maxWorldHeight, camera);
        stage = new Stage(viewport, batch);
        uiRenderer = new UIRenderer(viewport, stage);
    }

    protected void createUI() {
        Gdx.app.log("INIT", "AbstractScreen.createUI for " + getClass().getSimpleName());
        Gdx.input.setInputProcessor(stage);

        uiRootTable = new Table();
        uiRootTable.setFillParent(true);
        stage.addActor(uiRootTable);

        if (Main.DEBUG_UI)
            uiRootTable.debugAll();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
