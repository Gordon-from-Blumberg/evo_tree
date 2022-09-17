package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.animation.AnimatedParameterFloat;
import com.gordonfromblumberg.games.core.common.animation.GbAnimation;
import com.gordonfromblumberg.games.core.common.model.Cell;
import com.gordonfromblumberg.games.core.common.screens.FBORenderer;
import com.gordonfromblumberg.games.core.common.model.GameObject;

import java.util.Iterator;

public class GameWorldRenderer extends FBORenderer {
    private static final Color TEMP_COLOR = new Color();
    private static final Vector3 tempVec3 = new Vector3();

    private final GameWorld world;
    private final Batch batch;
    private Viewport viewport;
    private final Rectangle worldArea = new Rectangle();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Matrix3 viewToWorld = new Matrix3();
    private final Matrix3 worldToView = new Matrix3();

    final Array<ClickPoint> clickPoints = new Array<>();

    private final Color pauseColor = Color.GRAY;
    TextureRegion background;

    public GameWorldRenderer(GameWorld world, Batch batch, Viewport viewport) {
        super(viewport);

        this.batch = batch;
        this.world = world;
    }

    public void initialize(Viewport viewport, float width, float height) {
        final AssetManager assets = Main.getInstance().assets();

        this.viewport = viewport;
        worldArea.setSize(width, height);

        background = assets
                .get("image/texture_pack.atlas", TextureAtlas.class)
                .findRegion("background");

//        viewport.getCamera().position.set(l.getWidth() * l.getTileWidth() / 2f, 0, 0);
//        viewToWorld.set(new float[] {
//                 1.0f / l.getTileWidth(),  1.0f / l.getTileWidth(),  0.0f,
//                -1.0f / l.getTileHeight(), 1.0f / l.getTileHeight(), 0.0f,
//                 0.5f,                    -0.5f,                     1.0f
//        });
//        worldToView.set(viewToWorld).inv();

        world.onClick = this::click;
    }

    @Override
    public void render(float dt) {
//        batch.begin();
        final Color origColor = TEMP_COLOR.set(batch.getColor());
        if (world.paused) {
            batch.setColor(pauseColor);
        }

//        batch.draw(background, 0, 0);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int cellSize = world.cellSize;
        for (int i = 0, w = world.width; i < w; ++i) {
            for (int j = 0, h = world.height; j < h; ++j) {
                shapeRenderer.setColor((float) i / w, 0f, (float) j / h, 1f);
                shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0f, 0f, 0f, 1f);
        for (int i = 0, w = world.width; i < w; ++i) {
            for (int j = 0, h = world.height; j < h; ++j) {
                shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }

        final Iterator<ClickPoint> it = clickPoints.iterator();
        while (it.hasNext()) {
            ClickPoint cp = it.next();
            worldToScreen(tempVec3.set(cp.x, cp.y, 1));
            cp.animation.update(dt);
            final float circleMul = cp.getCircle();
            Gdx.app.log("RENDER", "render click at " + cp.x + ", " + cp.y + ", mul = " + circleMul);
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
     * Transforms viewport coordinates to isometric world
     */
    public void screenToWorld(Vector3 coords) {
        coords.z = 1.0f;
        coords.mul(viewToWorld);
    }

    /**
     * Transforms isometric world to viewport coordinates
     */
    public void worldToScreen(Vector3 coords) {
        coords.z = 1.0f;
        coords.mul(worldToView);
    }

    void click(float x, float y) {
        worldToScreen(tempVec3.set(x, y, 1));
        ClickPoint cp = ClickPoint.getInstance();
        clickPoints.add(cp.init(tempVec3.x, tempVec3.y));
    }
}
