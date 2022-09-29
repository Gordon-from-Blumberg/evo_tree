package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.event.EventProcessor;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.common.model.GameObject;
import com.gordonfromblumberg.games.core.common.utils.BiFloatConsumer;
import com.gordonfromblumberg.games.core.evotree.model.CellGrid;

public class GameWorld implements Disposable {
    private static int nextId = 1;
    private int turn = 0;

    private final Array<GameObject> gameObjects = new Array<>();

    private final EventProcessor eventProcessor = new EventProcessor();

    public Rectangle visibleArea;

    int sunLight;
    CellGrid cellGrid;
    int cellSize;

    boolean paused;
    private final Color pauseColor = Color.GRAY;
    final BitmapFontCache pauseText;

    private int maxCount = 0;

    private float updateDelay = 0.1f;
    private float time = 0;
    private int score = 0;

    BiFloatConsumer onClick;

    public GameWorld() {
        this(new GameWorldParams());
    }

    public GameWorld(GameWorldParams params) {
        visibleArea = new Rectangle();

        final AssetManager assets = Main.getInstance().assets();
        pauseText = new BitmapFontCache(assets.get("ui/uiskin.json", Skin.class).getFont("default-font"));

        cellGrid = new CellGrid(params.width, params.height);
        sunLight = params.sunLight;
        cellSize = params.cellSize;
    }

    public void initialize() {

    }

    public void setSize(int width, int height) {
//        this.width = width;
//        this.height = height;
        visibleArea.setSize(width, height);
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        gameObject.setGameWorld(this);
        gameObject.setActive(true);
        gameObject.setId(nextId++);
        if (gameObjects.size > maxCount) maxCount = gameObjects.size;
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjects.removeValue(gameObject, true);
        gameObject.release();
    }

    public Array<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void update(float delta) {
        if (!paused) {
            ++turn;

            time += delta;

            if (time < updateDelay) {
                return;
            }
            time = 0;

            cellGrid.updateSunLight(turn, sunLight);

            for (GameObject gameObject : gameObjects) {
                gameObject.update(delta);
            }

            eventProcessor.process();

            if (time > 2) {
                time = 0;
                Gdx.app.log("GameWorld", gameObjects.size + " objects in the world of maximum " + maxCount);
            }
        }
    }

    // world coords
    public void click(float x, float y) {
        if (onClick != null)
            onClick.accept(x, y);
    }

    //    public float getMinVisibleX() {
//        return visibleArea.x;
//    }
//
//    public float getMaxVisibleX() {
//        return visibleArea.x + visibleArea.width;
//    }
//
//    public float getMinVisibleY() {
//        return visibleArea.y;
//    }
//
//    public float getMaxVisibleY() {
//        return visibleArea.y + visibleArea.height;
//    }

    public void pause() {
        this.paused = !this.paused;
    }

    public void registerHandler(String type, EventHandler handler) {
        eventProcessor.registerHandler(type, handler);
    }

    public void pushEvent(Event event) {
        eventProcessor.push(event);
    }

    @Override
    public void dispose() {
        for (GameObject gameObject : gameObjects) {
            gameObject.dispose();
        }
    }
}
