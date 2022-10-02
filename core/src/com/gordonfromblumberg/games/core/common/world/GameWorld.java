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
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ClickHandler;
import com.gordonfromblumberg.games.core.common.model.GameObject;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.evotree.model.CellGrid;
import com.gordonfromblumberg.games.core.evotree.model.LightingTest;
import com.gordonfromblumberg.games.core.evotree.model.TreePart;

public class GameWorld implements Disposable {
    private static int nextId = 1;
    private int turn = 0;

    private final Array<GameObject> gameObjects = new Array<>();

    private final EventProcessor eventProcessor = new EventProcessor();

    public Rectangle visibleArea;

    int sunLight;
    CellGrid cellGrid;

    boolean paused;
    private final Color pauseColor = Color.GRAY;
    final BitmapFontCache pauseText;

    private int maxCount = 0;

    private float updateDelay = 0.1f;
    private float time = 0;
    private int score = 0;

    final Array<ClickHandler> clickHandlers = new Array<>(1);

    public GameWorld() {
        this(new GameWorldParams());
    }

    public GameWorld(GameWorldParams params) {
        Gdx.app.log("INIT", "GameWorld constructor");
        visibleArea = new Rectangle();

        final AssetManager assets = Main.getInstance().assets();
        pauseText = new BitmapFontCache(assets.get("ui/uiskin.json", Skin.class).getFont("default-font"));

        cellGrid = new CellGrid(params.width, params.height, params.cellSize);
        sunLight = params.sunLight;
    }

    public void initialize() {
        Gdx.app.log("INIT", "GameWorld init");
        if (AbstractFactory.getInstance().configManager().getBoolean("lightingTest")) {
            addClickHandler(this::testLighting);
        }
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

            cellGrid.updateSunLight(sunLight);

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
    public void click(int button, float x, float y) {
        for (ClickHandler handler : clickHandlers)
            handler.onClick(button, x, y);
    }

    public void addClickHandler(ClickHandler handler) {
        clickHandlers.add(handler);
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

    private void testLighting(int b, float x, float y) {
        Cell cell = cellGrid.findCell((int) x, (int) y);
        if (cell != null) {
            TreePart treePart = cell.getTreePart();
            if (treePart == null) {
                treePart = new LightingTest(5);
                cell.setTreePart(treePart);
            } else {
                int absorption = treePart.getLightAbsorption() + 5;
                if (absorption > 30) {
                    cell.setTreePart(null);
                } else {
                    ((LightingTest) treePart).setLightAbsorption(absorption);
                }
            }
        }
    }

    public Cell findCell(int x, int y) {
        return cellGrid.findCell(x, y);
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
