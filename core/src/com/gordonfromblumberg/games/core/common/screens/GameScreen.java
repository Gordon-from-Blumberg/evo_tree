package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.common.world.GameWorldRenderer;

public class GameScreen extends AbstractScreen {
    private GameWorld gameWorld;
    private GameWorldRenderer renderer;

    private final Vector2 coords2 = new Vector2();
    private final Vector3 coords3 = new Vector3();
    private final Vector3 viewCoords3 = new Vector3();
    private final Vector3 worldCoords3 = new Vector3();
    private Label cameraPos, zoom, screenCoord, viewCoord, worldCoord;

    protected GameScreen(SpriteBatch batch) {
        super(batch);
        Gdx.app.log("INIT", "GameScreen constructor");
        gameWorld = new GameWorld();
    }

    @Override
    public void initialize() {
        super.initialize();
        Gdx.app.log("INIT", "GameScreen init");
        gameWorld.initialize();
        worldRenderer = renderer = new GameWorldRenderer(gameWorld, batch, viewport);
        renderer.initialize();

        stage.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                x = Gdx.input.getX();
                y = Gdx.input.getY();
                screenCoord.setText(x + ", " + y);
                screenToViewport(x, y, viewCoords3);
                renderer.screenToWorld(worldCoords3.set(viewCoords3));
                renderer.click(Input.Buttons.LEFT, viewCoords3.x, viewCoords3.y);
                worldCoord.setText(viewCoords3.x + ", " + viewCoords3.y);
                gameWorld.click(Input.Buttons.LEFT, worldCoords3.x, worldCoords3.y);
            }
        });

        stage.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                x = Gdx.input.getX();
                y = Gdx.input.getY();
//                screenToViewport(x, y, viewCoords3);
//                renderer.screenToWorld(worldCoords3.set(viewCoords3));
//                renderer.click(Input.Buttons.LEFT, viewCoords3.x, viewCoords3.y);
                screenToWorld(x, y, worldCoords3);
                gameWorld.click(Input.Buttons.RIGHT, worldCoords3.x, worldCoords3.y);
            }
        });
    }

    void screenToViewport(float x, float y, Vector3 out) {
        viewport.unproject(coords3.set(x, y, 0));
//        viewCoord.setText(coords3.x + ", " + coords3.y);
        out.set(coords3);
    }

    void screenToWorld(float x, float y, Vector3 out) {
        screenToViewport(x, y, coords3);
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
//        if (Gdx.input.isKeyPressed(Input.Keys.UP))
//            camera.translate(0, cameraSpeed);
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
//            camera.translate(0, -cameraSpeed);

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
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
//        viewport = new ExtendViewport();
        viewport = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    protected void createUiRenderer() {
        Gdx.app.log("INIT", "GameScreen.createUiRenderer");
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
        uiRenderer = new GameUIRenderer(viewport, stage, gameWorld, this::screenToViewport);
    }

    @Override
    protected void createUI() {
        super.createUI();

        Gdx.app.log("INIT", "GameScreen.createUI");

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final GameUIRenderer renderer = (GameUIRenderer) uiRenderer;

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

        if (Main.DEBUG) {
            uiRootTable.add(createCoordsDebugTable(uiSkin))
                    .left().top();
        } else {
            uiRootTable.add();
        }

        uiRootTable.add(renderer.createInfoTable(uiSkin))
                .top().padLeft(10f);

        if (Main.DEBUG) {
            uiRootTable.add(renderer.createCellDebugTable(uiSkin))
                    .top().padLeft(10f);
        } else {
            uiRootTable.add();
        }

        uiRootTable.add(renderer.createDnaDesc(uiSkin))
                .expandX().top().right().padLeft(10f);

        uiRootTable.row();
        uiRootTable.add(renderer.createControlTable(uiSkin, configManager.getInteger("world.turnsPerSecond")))
                .expandY().left().bottom().pad(0f, 10f, 10f, 0f);
    }

    private Table createCoordsDebugTable(Skin uiSkin) {
        final Table table = UIUtils.createTable();
        table.add(new Label("Camera pos", uiSkin));
        table.add(cameraPos = new Label("Hello", uiSkin));

        table.row();
        table.add(new Label("Zoom", uiSkin));
        table.add(zoom = new Label("", uiSkin));

        table.row();
        table.add(new Label("Screen", uiSkin));
        table.add(screenCoord = new Label("", uiSkin));

        table.row();
        table.add(new Label("Viewport", uiSkin));
        table.add(viewCoord = new Label("", uiSkin));

        table.row();
        table.add(new Label("World", uiSkin));
        table.add(worldCoord = new Label("", uiSkin));
        return table;
    }
}
