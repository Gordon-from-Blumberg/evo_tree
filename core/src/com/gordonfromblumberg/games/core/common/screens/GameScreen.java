package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.common.world.GameWorldRenderer;

public class GameScreen extends AbstractScreen {
    private static final Logger log = LogManager.create(GameScreen.class);

    private GameWorld gameWorld;
    private GameWorldRenderer renderer;

    private final Vector3 viewCoords3 = new Vector3();
    private final Vector3 worldCoords3 = new Vector3();

    protected GameScreen(SpriteBatch batch) {
        super(batch);
        log.info("GameScreen constructor");
        gameWorld = new GameWorld();
    }

    @Override
    public void initialize() {
        super.initialize();

        log.info("GameScreen init");
        gameWorld.initialize();
        renderer.initialize();

        uiRenderer.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!event.isHandled()) {
                    x = Gdx.input.getX();
                    y = Gdx.input.getY();
                    ((GameUIRenderer) uiRenderer).click(Input.Buttons.LEFT, x, y);
                    renderer.screenToViewport(x, y, viewCoords3);
                    renderer.viewToWorld(viewCoords3.x, viewCoords3.y, worldCoords3);
                    gameWorld.click(Input.Buttons.LEFT, worldCoords3.x, worldCoords3.y);
                }
            }
        });

        uiRenderer.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!event.isHandled()) {
                    x = Gdx.input.getX();
                    y = Gdx.input.getY();
//                screenToViewport(x, y, viewCoords3);
//                renderer.screenToWorld(worldCoords3.set(viewCoords3));
//                renderer.click(Input.Buttons.LEFT, viewCoords3.x, viewCoords3.y);
//                screenToWorld(x, y, worldCoords3);
//                gameWorld.click(Input.Buttons.RIGHT, worldCoords3.x, worldCoords3.y);
                }
            }
        });
    }

    @Override
    protected void update(float delta) {
        super.update(delta);
        gameWorld.update(delta);        // update game state
    }

    @Override
    protected void createWorldRenderer() {
        super.createWorldRenderer();
        log.info("GameScreen.createWorldRenderer");

        worldRenderer = renderer = new GameWorldRenderer(gameWorld, batch);
    }

    @Override
    protected void createUiRenderer() {
        log.info("GameScreen.createUiRenderer");
        final GameUIRenderer uiRenderer = new GameUIRenderer(batch, gameWorld,
                this.renderer::screenToViewport, this.renderer::viewToWorld, this::getWorldCameraParams);
        this.uiRenderer = uiRenderer;

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();

        final float minZoom = configManager.getFloat("minZoom");
        final float maxZoom = configManager.getFloat("maxZoom");
        final OrthographicCamera camera = renderer.getCamera();
        uiRenderer.addListener(new InputListener() {
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

        uiRenderer.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    gameWorld.pause();
                    return true;
                }
                return false;
            }
        });
    }

    private void getWorldCameraParams(GameUIRenderer.WorldCameraParams params) {
        params.position.set(renderer.getCamera().position);
        params.zoom = renderer.getCamera().zoom;
    }

    @Override
    public void dispose() {
        gameWorld.dispose();

        super.dispose();
    }
}
