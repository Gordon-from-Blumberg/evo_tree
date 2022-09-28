package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.common.world.GameWorldRenderer;

public class GameScreen extends AbstractScreen {
    private GameWorld gameWorld;
    private GameWorldRenderer renderer;

    private final Vector2 coords2 = new Vector2();
    private final Vector3 coords3 = new Vector3();
    private Label cameraPos, zoom, screenCoord, viewCoord, worldCoord;

    protected GameScreen(SpriteBatch batch) {
        super(batch);

        gameWorld = new GameWorld();
    }

    @Override
    public void initialize() {
        super.initialize();

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        gameWorld.initialize();
        worldRenderer = renderer = new GameWorldRenderer(gameWorld, batch, viewport);
        renderer.initialize(viewport, viewport.getWorldHeight(), viewport.getWorldHeight());

        final float minZoom = configManager.getFloat("minZoom");
        final float maxZoom = configManager.getFloat("maxZoom");
        stage.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (amountY > 0)
                    camera.zoom *= 1.25f;
                else if (amountY < 0)
                    camera.zoom /= 1.25f;
                if (camera.zoom < minZoom)
                    camera.zoom = minZoom;
                if (camera.zoom > maxZoom)
                    camera.zoom = maxZoom;
                return true;
            }
        });

        stage.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                x = Gdx.input.getX();
                y = Gdx.input.getY();
                screenCoord.setText(x + ", " + y);
                screenToWorld(x, y, coords3);
                worldCoord.setText(coords3.x + ", " + coords3.y);
                gameWorld.click(coords3.x, coords3.y);
            }
        });

//        stage.addListener(new ClickListener(Input.Buttons.RIGHT) {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//
//            }
//        });
    }

    void screenToWorld(float x, float y, Vector3 out) {
        viewport.unproject(coords3.set(x, y, 0));
        viewCoord.setText(coords3.x + ", " + coords3.y);
        renderer.screenToWorld(coords3);
        out.set(coords3);
    }

    @Override
    protected void update(float delta) {
        float cameraSpeed = 8 * camera.zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.translate(-cameraSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.translate(cameraSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            camera.translate(0, cameraSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            camera.translate(0, -cameraSpeed);

        super.update(delta);            // apply camera moving and update batch projection matrix
        gameWorld.update(delta);        // update game state
        cameraPos.setText(camera.position.x + ", " + camera.position.y);
        zoom.setText("" + camera.zoom);
    }

    @Override
    public void dispose() {
        gameWorld.dispose();

        super.dispose();
    }

    @Override
    protected void createWorldViewport() {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final float worldWidth = configManager.getFloat("worldWidth");
        final float minRatio = configManager.getFloat("minRatio");
        final float maxWorldHeight = worldWidth / minRatio;
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new FillViewport(worldWidth, maxWorldHeight, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    protected void createUI() {
        super.createUI();

        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    gameWorld.pause();
                    return true;
                }
                return false;
            }
        });

        cameraPos = new Label("Hello", uiSkin);
        zoom = new Label("", uiSkin);
        screenCoord = new Label("", uiSkin);
        viewCoord = new Label("", uiSkin);
        worldCoord = new Label("", uiSkin);

        uiRootTable.add(new Label("Camera pos", uiSkin));
        uiRootTable.add(cameraPos);
        uiRootTable.row();
        uiRootTable.add(new Label("Zoom", uiSkin));
        uiRootTable.add(zoom);
        uiRootTable.row();
        uiRootTable.add(new Label("Screen", uiSkin));
        uiRootTable.add(screenCoord);
        uiRootTable.row();
        uiRootTable.add(new Label("Viewport", uiSkin));
        uiRootTable.add(viewCoord);
        uiRootTable.row();
        uiRootTable.add(new Label("World", uiSkin));
        uiRootTable.add(worldCoord);
        uiRootTable.row();
        uiRootTable.add();
        uiRootTable.add();
        uiRootTable.add().expand();
    }
}
