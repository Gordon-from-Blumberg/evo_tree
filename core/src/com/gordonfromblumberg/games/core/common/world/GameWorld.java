package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.event.EventProcessor;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ClickHandler;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.evotree.model.*;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

import java.util.Iterator;

public class GameWorld implements EvoTreeWorld, Disposable {
    private static final Logger log = LogManager.create(GameWorld.class);
    private static final int LIGHT_SOURCE_MOVE_DELAY = 10;
    private static final int MIN_GAME_OBJECTS = 30;
    private static final int ADD_RANDOM_SEED_DELAY = 50;
    private static int nextTreeId = 1;
    private static int nextSeedId = 1;
    private int turn = 0;
    private int lastSeedAddedTurn = 0;

    private final Array<Seed> seeds = new Array<>();
    private final Array<Tree> trees = new Array<>();
    private Tree selectedTree;

    private final EventProcessor eventProcessor = new EventProcessor();

    CellGrid cellGrid;
    private SimpleLightDistribution simpleLightDistribution;
    LightDistribution lightDistribution;

    GeneticRules geneticRules = new GeneticRules();

    boolean running;
    boolean paused;
//    final BitmapFontCache pauseText;

    private int maxSeeds = 0;
    private int maxTrees = 0;
    private int maxGeneration = 0;

    private float updateDelay = 0.10f;
    private float time = 0;

    final Array<ClickHandler> clickHandlers = new Array<>(1);

    public GameWorld(GameWorldParams params) {
        log.info("GameWorld constructor");

        final AssetManager assets = Main.getInstance().assets();
//        pauseText = new BitmapFontCache(assets.get("ui/uiskin.json", Skin.class).getFont("default-font"));

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        cellGrid = new CellGrid(params.width, params.height,
                configManager.getInteger("world.cellSize"),
                configManager.getInteger("world.chunkSize"));
        simpleLightDistribution = new SimpleLightDistribution(params.width, params.height, params.sunLight, params.lightAbsorptionStep);
        lightDistribution = params.decorate(simpleLightDistribution);

        for (int i = 0, n = params.getLightSourcesCount(); i < n; ++i) {
            LightSource lightSource = new LightSource(params.getLightSourceStrength());
            cellGrid.addCellObject(lightSource, i, params.getHeight() - 1);
            log.info("Created light source with strength " + params.getLightSourceStrength() + " at " + i + ", " + (params.getHeight() - 1));
        }
    }

    public void initialize() {
        log.info("GameWorld init");
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        if (Main.LIGHTING_TEST) {
            addClickHandler(this::testLighting);
        } else {
            addClickHandler(this::selectTree);
        }

        if (configManager.contains("world.turnsPerSecond"))
            updateDelay = 1f / configManager.getInteger("world.turnsPerSecond");

        if (!Main.LIGHTING_TEST) {
            for (int i = 5, w = cellGrid.getWidth(); i < w; i += 5) {
                Seed seed = Seed.getInstance();
                seed.init();
                cellGrid.addCellObject(seed, i, RandomGen.INSTANCE.nextInt(cellGrid.getHeight() / 2));
                seed.setGeneration(1);
                seed.setEnergy(4000);
                addSeed(seed);
            }
        }

        running = true;
        log.debug("Game world initialized");
    }

    public void setSize(int width, int height) {
//        this.width = width;
//        this.height = height;
    }

    @Override
    public void addSeed(Seed seed) {
        seeds.add(seed);
        seed.setId(nextSeedId++);
        if (seeds.size > maxSeeds) maxSeeds = seeds.size;
        if (seed.getGeneration() > maxGeneration) maxGeneration = seed.getGeneration();
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
    public CellGrid getGrid() {
        return cellGrid;
    }

    @Override
    public GeneticRules getGeneticRules() {
        return geneticRules;
    }

    public void update(float delta) {
        if (running && !paused) {
            time += delta;
            if (time < updateDelay) {
                return;
            }
            time = 0;

            ++turn;

            lightDistribution.nextTurn();

            CellGrid grid = this.cellGrid;
            Iterator<Seed> seedIterator = seeds.iterator();
            while (seedIterator.hasNext()) {
                Seed seed = seedIterator.next();
                if (seed.update(this)) {
                    seedIterator.remove();
                    grid.removeCellObject(seed);
                    seed.release();
                }
            }

            Iterator<Tree> treeIterator = trees.iterator();
            while (treeIterator.hasNext()) {
                Tree tree = treeIterator.next();
                if (tree.update(this)) {
                    treeIterator.remove();
                    tree.release();
                    if (tree == selectedTree) {
                        selectTree(null);
                    }
                }
            }

            if (turn % LIGHT_SOURCE_MOVE_DELAY == 0) {
                cellGrid.moveLightSources();
            }
            cellGrid.updateSunLight(lightDistribution);

            eventProcessor.process();

//            if (turn % 30 == 0) {
//                Gdx.app.log("GameWorld", seeds.size + " seeds in the world of maximum " + maxSeeds);
//                Gdx.app.log("GameWorld", trees.size + " trees in the world of maximum " + maxTrees);
//            }

            int dropRandomSeeds = MIN_GAME_OBJECTS - trees.size - seeds.size;
            if (dropRandomSeeds > 0 || turn - lastSeedAddedTurn >= ADD_RANDOM_SEED_DELAY) {
                addRandomSeeds(dropRandomSeeds > 0 ? dropRandomSeeds : 1);
                lastSeedAddedTurn = turn;
            }

            if (seeds.isEmpty() && trees.isEmpty() && !Main.LIGHTING_TEST) {
                running = false;
            }
        }
    }

    private void addRandomSeeds(int count) {
        final int y = cellGrid.getHeight() - 1;
        while (count-- > 0) {
            Seed seed = Seed.getInstance();
            seed.initRandom();
            seed.setGeneration(1);
            seed.setEnergy(4000);
            addSeed(seed);
            int x = RandomGen.INSTANCE.nextInt(cellGrid.getWidth());
            Cell cell = cellGrid.cells[x][y];
            while (cell.getObject() != null) {
                cell = cellGrid.getCell(cell, Direction.right);
            }
            cellGrid.addCellObject(seed, cell);
        }
    }

    public int getTurn() {
        return turn;
    }

    public int getSeedCount() {
        return seeds.size;
    }

    public int getMaxSeeds() {
        return maxSeeds;
    }

    public int getTreeCount() {
        return trees.size;
    }

    public int getMaxTrees() {
        return maxTrees;
    }

    public int getMaxGeneration() {
        return maxGeneration;
    }

    public int getSunLight() {
        return lightDistribution.getLight(0, cellGrid.getHeight() - 1);
    }

    public void setTurnsPerSecond(int turnsPerSecond) {
        updateDelay = 1f / turnsPerSecond;
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

    private void selectTree(int b, float x, float y) {
        Cell cell = cellGrid.findCell((int) x, (int) y);
        if (cell != null) {
            CellObject treePart = cell.getObject();
            if (treePart instanceof TreePart) {
                selectTree(((TreePart) treePart).getTree());
                return;
            }
        }
        selectTree(null);
    }

    private void selectTree(Tree tree) {
        if (selectedTree != tree) {
            selectedTree = tree;
//            eventProcessor.push(SelectTreeEvent.getInstance().setTree(tree));
        }
    }

    private void testLighting(int b, float x, float y) {
        Cell cell = cellGrid.findCell((int) x, (int) y);
        if (cell != null) {
            CellObject cellObject = cell.getObject();
            if (cellObject == null) {
                cellObject = new LightingTest(5);
                cell.setObject(cellObject);
            } else {
                int absorption = cellObject.getLightAbsorption() + 5;
                if (absorption > 30) {
                    cell.setObject(null);
                } else {
                    ((LightingTest) cellObject).setLightAbsorption(absorption);
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
