package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.event.EventProcessor;
import com.gordonfromblumberg.games.core.common.screens.AbstractScreen;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.model.GameObject;
import com.gordonfromblumberg.games.core.common.utils.BSPTree;

import java.util.Iterator;

public class GameWorld implements Disposable {
    private static int nextId = 1;

    private final Array<GameObject> gameObjects = new Array<>();

    private final BSPTree tree;
    private final EventProcessor eventProcessor = new EventProcessor();

    public Rectangle visibleArea;
    private float width, height;

    boolean paused;
    private final Color pauseColor = Color.GRAY;
    final BitmapFontCache pauseText;

    private int maxCount = 0;

    private float time = 0;
    private int score = 0;

    public GameWorld() {
        final AssetManager assets = Main.getInstance().assets();

        visibleArea = new Rectangle();
        tree = new BSPTree(0, 0, 0, 0);

        pauseText = new BitmapFontCache(assets.get("ui/uiskin.json", Skin.class).getFont("default-font"));
        pauseText.setText("PAUSE", 100, 100);
    }
    public void initialize() {
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
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
            time += delta;
//          visibleArea.set(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
//          tree.resetAndMove(0, 0);

            for (GameObject gameObject : gameObjects) {
                gameObject.update(delta);
                if (gameObject.isActive()) {
//                  tree.addObject(gameObject);
                }
            }

            detectCollisions();

            eventProcessor.process();

            if (time > 2) {
                time = 0;
                Gdx.app.log("GameWorld", gameObjects.size + " objects in the world of maximum " + maxCount);
            }
        }
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

    private void detectCollisions() {
        while (tree.hasNext()) {
            final Iterator<GameObject> iterator = tree.next();
            final Iterator<GameObject> internalIterator = tree.internalIterator();
            while (iterator.hasNext()) {
                final GameObject gameObject = iterator.next();
                if (!gameObject.isActive())
                    continue;

                while (internalIterator.hasNext()) {
                    final GameObject internalGameObject = internalIterator.next();
                    if (!internalGameObject.isActive())
                        continue;

                    if (detectCollision(gameObject, internalGameObject)) {
                        gameObject.collide(internalGameObject);
                    }
                }
            }
        }
    }

    private boolean detectCollision(GameObject obj1, GameObject obj2) {
        return obj1.getBoundingRectangle().overlaps(obj2.getBoundingRectangle())
                && Intersector.intersectPolygons(obj1.getPolygon(), obj2.getPolygon(), null);
    }

    @Override
    public void dispose() {
        for (GameObject gameObject : gameObjects) {
            gameObject.dispose();
        }
    }
}