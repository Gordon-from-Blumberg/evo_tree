package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.screens.FBORenderer;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evotree.model.*;

import java.util.Iterator;

public class GameWorldRenderer extends FBORenderer {
    private static final Logger log = LogManager.create(GameWorldRenderer.class);

    private static final Color LIGHT_COLOR = new Color(0.4f, 0.8f, 1f, 1f);
    private static final Color DARK_COLOR = new Color(0f, 0.12f, 0.07f, 1f);
    private static final Color MID_COLOR = new Color();
    private static final float MAX_SUN_LIGHT;
    private static final Color SEED_COLOR = new Color(0.66f, 0.41f, 0.19f, 1f);

    private static final Color MIN_ABS_COLOR = new Color(0.5f, 1f, 0.7f, 1f);
    private static final Color MAX_ABS_COLOR = new Color(0f, 0.6f, 0.1f, 1f);
    private static final float MAX_ABSORPTION;

    private static final Vector3 tempVec3 = new Vector3();

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        configManager.getColor("world.lightColor", LIGHT_COLOR);
        configManager.getColor("world.darkColor", DARK_COLOR);
        MID_COLOR.set(DARK_COLOR).lerp(LIGHT_COLOR, configManager.getFloat("world.midColor"));
        MAX_SUN_LIGHT = configManager.getInteger("world.sunLight");
        configManager.getColor("seed.color", SEED_COLOR);

        // lighting test
        configManager.getColor("world.lightingTest.minAbsColor", MIN_ABS_COLOR);
        configManager.getColor("world.lightingTest.maxAbsColor", MAX_ABS_COLOR);
        MAX_ABSORPTION = configManager.getInteger("world.lightingTest.maxAbsorption");
    }

    private final GameWorld world;
    private final Batch batch;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Matrix3 viewToWorld = new Matrix3();
    private final Matrix3 worldToView = new Matrix3();

    final Array<ClickPoint> clickPoints = new Array<>();

    private final Color pauseColor = Color.GRAY;

    public GameWorldRenderer(GameWorld world, Batch batch) {
        super();
        log.info("GameWorldRenderer constructor");
        this.batch = batch;
        this.world = world;
    }

    public void initialize() {
        log.info("GameWorldRenderer init");
        final AssetManager assets = Main.getInstance().assets();

        float worldHeight = world.cellGrid.getHeight() * world.cellGrid.getCellSize();
        if (worldHeight > viewport.getWorldHeight()) {
            float ration = viewport.getWorldWidth() / viewport.getWorldHeight();
            viewport.setWorldSize(ration * worldHeight, worldHeight);
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
    }

    @Override
    public void render(float dt) {
        updateCamera();

        if (world.paused) {
            batch.setColor(pauseColor);
        }

        final ShapeRenderer shapeRenderer = this.shapeRenderer;
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int cellSize = world.cellGrid.getCellSize();
        Cell[][] cells = world.cellGrid.cells;
        final float nightR = DARK_COLOR.r, nightG = DARK_COLOR.g, nightB = DARK_COLOR.b;
        final float diffR = LIGHT_COLOR.r - DARK_COLOR.r,
                diffG = LIGHT_COLOR.g - DARK_COLOR.g,
                diffB = LIGHT_COLOR.b - DARK_COLOR.b;
        final float absR = MAX_ABS_COLOR.r, absG = MAX_ABS_COLOR.g, absB = MAX_ABS_COLOR.b;
        final float daR = MIN_ABS_COLOR.r - MAX_ABS_COLOR.r,
                daG = MIN_ABS_COLOR.g - MAX_ABS_COLOR.g,
                daB = MIN_ABS_COLOR.b - MAX_ABS_COLOR.b;
        for (int i = 0, w = world.cellGrid.getWidth(); i < w; ++i) {
            for (int j = 0, h = world.cellGrid.getHeight(); j < h; ++j) {
                final Cell cell = cells[i][j];
                final TreePart treePart = cell.getTreePart();
                if (treePart == null) {
                    float k = cell.getSunLight() / MAX_SUN_LIGHT;
                    shapeRenderer.setColor(
                            nightR + diffR * k,
                            nightG + diffG * k,
                            nightB + diffB * k,
                            1);
                } else {
                    if (treePart instanceof Seed) {
                        shapeRenderer.setColor(SEED_COLOR);
                    } else if (treePart instanceof Wood) {
                        Color treeColor = ((Wood) treePart).getTree().getColor();
                        float k = (treePart instanceof Shoot) ? 1.25f : 1;
                        float o = treePart.getLightAbsorption() / 44f * (1f - 0.2f) + 0.2f;
                        if (o > 1f) o = 1f;
                        shapeRenderer.setColor(
                                (k * treeColor.r - MID_COLOR.r) * o + MID_COLOR.r,
                                (k * treeColor.g - MID_COLOR.g) * o + MID_COLOR.g,
                                (k * treeColor.b - MID_COLOR.b) * o + MID_COLOR.b,
                                1f);
                    } else {
                        float k = 1 - treePart.getLightAbsorption() / MAX_ABSORPTION;
                        shapeRenderer.setColor(
                                absR + daR * k,
                                absG + daG * k,
                                absB + daB * k,
                                1);
                    }
                }
                shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glLineWidth(1f / ((OrthographicCamera) viewport.getCamera()).zoom);
        for (int i = 0, w = world.cellGrid.getWidth(); i < w; ++i) {
            Cell[] col = cells[i];
            for (int j = 0, h = world.cellGrid.getHeight(); j < h; ++j) {
                TreePart treePart = col[j].getTreePart();
                if (treePart != null) {
                    if (treePart instanceof Shoot) {
                        shapeRenderer.setColor(0.4f, 0.3f, 0f, 1f);
                    } else {
                        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                    }
                    shapeRenderer.rect(i * cellSize + 1, j * cellSize + 1, cellSize - 2, cellSize - 2);
                }
            }
        }

        final Iterator<ClickPoint> it = clickPoints.iterator();
        while (it.hasNext()) {
            ClickPoint cp = it.next();
            worldToView(tempVec3.set(cp.x, cp.y, 1));
            cp.animation.update(dt);
            final float circleMul = cp.getCircle();
            log.debug("Render click at " + cp.x + ", " + cp.y + ", mul = " + circleMul);
            final float clickWidth = circleMul * ClickPoint.WIDTH;
            final float clickHeight = circleMul * ClickPoint.HEIGHT;
            shapeRenderer.ellipse(cp.x - clickWidth / 2, cp.y - clickHeight / 2, clickWidth, clickHeight);

            if (cp.animation.isFinished()) {
                it.remove();
                cp.release();
            }
        }
        shapeRenderer.end();

//        batch.begin();
//
//        if (world.paused) {
//            world.pauseText.draw(batch);
//            batch.setColor(origColor);
//        }
//
//        batch.end();
    }

    /**
     * Transforms viewport coordinates to logical world
     */
    public void viewToWorld(float x, float y, Vector3 out) {
        out.set(x, y, 1f).mul(viewToWorld);
    }

    /**
     * Transforms logical world to viewport coordinates
     */
    public void worldToView(Vector3 coords) {
        coords.z = 1.0f;
        coords.mul(worldToView);
    }

    private void updateCamera() {
        float cameraSpeed = 8 * camera.zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.translate(-cameraSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.translate(cameraSpeed, 0);
//        if (Gdx.input.isKeyPressed(Input.Keys.UP))
//            camera.translate(0, cameraSpeed);
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
//            camera.translate(0, -cameraSpeed);

        camera.update();
    }
}
