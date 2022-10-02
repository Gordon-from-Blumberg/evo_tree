package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.event.EventProcessor;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ClickHandler;
import com.gordonfromblumberg.games.core.evotree.model.*;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

import java.util.Iterator;

public class GameWorld implements EvoTreeWorld, Disposable {
    private static int nextTreeId = 1;
    private static int nextPartId = 1;
    private int turn = 0;

    private final Array<Seed> seeds = new Array<>();
    private final Array<Tree> trees = new Array<>();

    private final EventProcessor eventProcessor = new EventProcessor();

    int sunLight;
    CellGrid cellGrid;

    boolean paused;
    private final Color pauseColor = Color.GRAY;
    final BitmapFontCache pauseText;

    private int maxSeeds = 0;
    private int maxTrees = 0;

    private float updateDelay = 0.1f;
    private float time = 0;

    final Array<ClickHandler> clickHandlers = new Array<>(1);

    public GameWorld() {
        this(new GameWorldParams());
    }

    public GameWorld(GameWorldParams params) {
        Gdx.app.log("INIT", "GameWorld constructor");

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
    }

    @Override
    public void addSeed(Seed seed) {
        seeds.add(seed);
        seed.setId(nextPartId++);
        if (seeds.size > maxSeeds) maxSeeds = seeds.size;
    }

    @Override
    public void removeSeed(Seed seed) {
        seeds.removeValue(seed, true);
        seed.release();
    }

    @Override
    public void addTree(Tree tree) {
        trees.add(tree);
        tree.setId(nextTreeId++);
        if (trees.size > maxTrees) maxTrees = trees.size;
    }

    @Override
    public void removeTree(Tree tree) {
        trees.removeValue(tree, true);
        tree.release();
    }

    @Override
    public CellGrid getGrid() {
        return cellGrid;
    }

    @Override
    public int nextPartId() {
        return nextPartId++;
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

            Iterator<Seed> seedIterator = seeds.iterator();
            while (seedIterator.hasNext()) {
                Seed seed = seedIterator.next();
                if (seed.update(this)) {
                    seedIterator.remove();
                    seed.release();
                }
            }

            Iterator<Tree> treeIterator = trees.iterator();
            while (treeIterator.hasNext()) {
                Tree tree = treeIterator.next();
                if (tree.update(this)) {
                    treeIterator.remove();
                    tree.release();
                }
            }

            eventProcessor.process();

            if (turn % 20 == 0) {
                Gdx.app.log("GameWorld", seeds.size + " seeds in the world of maximum " + maxSeeds);
                Gdx.app.log("GameWorld", trees.size + " trees in the world of maximum " + maxTrees);
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

    }
}
