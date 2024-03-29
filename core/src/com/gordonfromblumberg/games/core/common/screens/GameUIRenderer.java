package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.SnapshotArray;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.ui.*;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.CoordsConverter;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.evotree.event.SelectTreeEvent;
import com.gordonfromblumberg.games.core.evotree.model.*;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.evotree.model.Tree;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.padLeft;

public class GameUIRenderer extends UIRenderer {
    private static final String DEFAULT_SAVE_DIR = "saves";
    private static final String TREE_EXTENSION = "tree";
    private static final Logger log = LogManager.create(GameUIRenderer.class);

    private GameWorld world;

    private final Vector3 GAME_VIEW_COORDS = new Vector3();
    private final Vector3 CLICK_COORDS = new Vector3();
    private final WorldCameraParams WORLD_CAMERA_PARAMS = new WorldCameraParams();
    private final CoordsConverter toGameView;
    private final CoordsConverter toGameWorld;
    private Label screenCoord, viewCoord, worldCoord;
    private final Consumer<WorldCameraParams> worldCameraParamsGetter;
    private Cell cell;
    private SaveLoadWindow saveWindow, loadWindow;

    public GameUIRenderer(SpriteBatch batch, GameWorld world,
                          CoordsConverter toGameView, CoordsConverter toGameWorld,
                          Consumer<WorldCameraParams> worldCameraParamsGetter) {
        super(batch);

        log.info("GameUIRenderer constructor");

        this.world = world;
        this.toGameView = toGameView;
        this.toGameWorld = toGameWorld;
        this.worldCameraParamsGetter = worldCameraParamsGetter;

        init();
    }

    void click(int button, float screenX, float screenY) {
        if (Main.DEBUG) {
            screenCoord.setText(screenX + ", " + screenY);
            toGameView.convert(screenX, screenY, CLICK_COORDS);
            toGameWorld.convert(CLICK_COORDS.x, CLICK_COORDS.y, CLICK_COORDS);
            worldCoord.setText(CLICK_COORDS.x + ", " + CLICK_COORDS.y);
        }
    }

    private void init() {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final AssetManager assets = Main.getInstance().assets();
        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);

        if (Main.DEBUG) {
            rootTable.add(createCoordsDebugTable(uiSkin))
                    .left().top();
        } else {
            rootTable.add();
        }

        rootTable.add(createInfoTable(uiSkin))
                .top().padLeft(10f);

        if (Main.DEBUG) {
            rootTable.add(createCellDebugTable(uiSkin))
                    .top().padLeft(10f);
        } else {
            rootTable.add();
        }

        rootTable.add(new Label("", uiSkin))
//        rootTable.add(createDnaDesc(uiSkin))
                .expandX().top().right().padLeft(10f);

        rootTable.row();
        rootTable.add(createControlTable(uiSkin, configManager.getInteger("world.turnsPerSecond")))
                .expandY().left().bottom().pad(0f, 10f, 10f, 0f);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F5 && (loadWindow == null || !loadWindow.isVisible())) {
                    Consumer<ByteBuffer> saveHandler = bb -> {
                        bb.put((byte) 1);
                        bb.putChar('H');
                        bb.putChar('i');
                        bb.putLong(System.currentTimeMillis());
                    };
                    if (saveWindow == null) {
                        saveWindow = createSaveLoadWindow(false, uiSkin);
                        saveWindow.open(saveHandler);
                    } else {
                        saveWindow.toggle(saveHandler);
                    }
                    return true;
                } else if (keycode == Input.Keys.F6 && (saveWindow == null || !saveWindow.isVisible())) {
                    Consumer<ByteBuffer> loadHandler = bb -> {
                        while (bb.hasRemaining()) {
                            log.debug(String.valueOf(bb.get()));
                        }
                    };
                    if (loadWindow == null) {
                        loadWindow = createSaveLoadWindow(true, uiSkin);
                        loadWindow.open(loadHandler);
                    } else {
                        loadWindow.toggle(loadHandler);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private Table createCoordsDebugTable(Skin uiSkin) {
        final Table table = UIUtils.createTable();
        table.add(new Label("Camera pos", uiSkin));
        table.add(new UpdatableLabel(uiSkin, () -> WORLD_CAMERA_PARAMS.position.x + ", " + WORLD_CAMERA_PARAMS.position.y));

        table.row();
        table.add(new Label("Zoom", uiSkin));
        table.add(new UpdatableLabel(uiSkin, () -> WORLD_CAMERA_PARAMS.zoom));

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

    Table createInfoTable(Skin uiSkin) {
        float pad = 10f;
        Table table = UIUtils.createTable();
        table.add(new Label("Turn", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> world.getTurn()))
                .minWidth(80);

        table.row();
        table.add(new Label("Light", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> world.getSunLight()))
                .left();

        table.row();
        table.add(new Label("Seeds", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> world.getSeedCount() + " of " + world.getMaxSeeds()))
                .left();

        table.row();
        table.add(new Label("Trees", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> world.getTreeCount() + " of " + world.getMaxTrees()))
                .left();

        table.row();
        table.add(new Label("Generation", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> world.getMaxGeneration()))
                .left();

        return table;
    }

    Table createCellDebugTable(Skin uiSkin) {
        float pad = 10f;
        Table table = UIUtils.createTable();
        table.add(new Label("Cell", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> cell != null ? cell.getX() + ", " + cell.getY() : "No cell"))
                .left();

        table.row();
        table.add(new Label("Light", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> cell != null ? cell.getSunLight() + " / " + cell.isUnderSun() : "No cell"))
                .left();

        table.row();
        table.add(new Label("Absorption", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> cell != null && cell.getObject() != null
                ? cell.getObject().getLightAbsorption() : "No tree part")
        )
                .left();

        table.row();
        table.add(new Label("Energy", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> cell != null && cell.getObject() instanceof TreePart
                ? ((TreePart) cell.getObject()).calcEnergy(world.getGrid()) : "No tree part")
        )
                .left();

        table.row();
        table.add(new Label("Life time (rest)", uiSkin))
                .padRight(pad).right();
        final Function<Tree, String> lifeTime = tree -> tree.getLifetime() + " (" + tree.getRestLifeTime() + ")";
        table.add(new UpdatableLabel(uiSkin, () -> cell != null && cell.getObject() instanceof TreePart
                        ? lifeTime.apply(((TreePart) cell.getObject()).getTree()) : "No tree part")
                )
                .left();

        table.row();
        table.add(new Label("Tree", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
                    CellObject cellObject = cell != null ? cell.getObject() : null;
                    if (cellObject instanceof Seed) {
                        return "Seed #" + ((Seed) cellObject).getId();
                    }
                    if (cellObject instanceof TreePart) {
                        TreePart treePart = (TreePart) cellObject;
                        return "#" + treePart.getTree().getId() + ", " + treePart.getType();
                    }
                    return cellObject != null ? cellObject.getClass().getSimpleName() : "No tree";
                }))
                .minWidth(160);

        table.row();
        table.add(new Label("Tree energy", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
            CellObject treePart = cell != null ? cell.getObject() : null;
            if (treePart instanceof TreePart) {
                return  ((TreePart) treePart).getTree().getEnergy();
            }
            return "No tree";
        }))
                .left();

        table.row();
        table.add(new Label("Tree size", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
                    CellObject treePart = cell != null ? cell.getObject() : null;
                    if (treePart instanceof TreePart) {
                        return  ((TreePart) treePart).getTree().getSize();
                    }
                    return "No tree";
                }))
                .left();

        return table;
    }

    Table createControlTable(Skin uiSkin, int initialValue) {
        float pad = 10f;
        Table table = UIUtils.createTable();
        table.add(new Label("Turns per sec", uiSkin))
                .padRight(pad).right();

        IntChangeableLabel speedControl = new IntChangeableLabel(uiSkin, world::setTurnsPerSecond);
        speedControl.setMinValue(4);
        speedControl.setMaxValue(64);
        speedControl.setFieldWidth(30);
        speedControl.geometric();
        speedControl.setStep(2);
        speedControl.setValue(initialValue);
        table.add(speedControl)
                .left();
        return table;
    }

    WidgetGroup createDnaDesc(Skin uiSkin) {
        VerticalGroup group = new VerticalGroup();
        for (int i = 0, n = DNA.GENES_COUNT; i < n; ++i) {
            group.addActor(new Label("", uiSkin));
        }

        world.registerHandler("selectTree", new EventHandler() {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public boolean handle(Event event) {
                Tree tree = ((SelectTreeEvent) event).getTree();
                if (tree == null) {
                    group.setVisible(false);
                } else {
                    DNA dna = tree.getDna();
                    SnapshotArray<Actor> labels = group.getChildren();
                    Actor[] labelArr = labels.begin();
                    for (int i = 0, n = DNA.GENES_COUNT; i < n; ++i) {
                        Gene gene = dna.getGene(i);
                        sb.delete(0, sb.length());
                        sb.append("     ").append(padLeft(gene.getValue(Direction.up), 2))
                                .append("    ")
                                .append(padLeft(gene.getValue(Gene.LIGHT_ABSORPTION), 2))
                                .append('\n')

                                .append(padLeft(i, 2)).append(" ")
                                .append(padLeft(gene.getValue(Direction.left), 2)).append("  ")
                                .append(padLeft(gene.getValue(Direction.right), 2))
                                .append("  ")
                                .append(padLeft(gene.getValue(Gene.CONDITION1), 2)).append(" ")
                                .append(padLeft(gene.getValue(Gene.PARAMETER1), 2)).append(" ")
//                                .append(padLeft(gene.getValue(Gene.MOVE_TO), 2))
                                .append('\n')

                                .append("     ").append(padLeft(gene.getValue(Direction.down), 2))
                                .append("    ")
                                .append(padLeft(gene.getValue(Gene.CONDITION2), 2)).append(" ")
                                .append(padLeft(gene.getValue(Gene.PARAMETER2), 2));

                        ((Label) labelArr[i]).setText(sb.toString());
                    }
                    group.setVisible(true);
                }
                return true;
            }
        });

        return group;
    }

    private SaveLoadWindow createSaveLoadWindow(boolean load, Skin skin) {
        ConfigManager config = AbstractFactory.getInstance().configManager();
        SaveLoadWindow window = new SaveLoadWindow(
                stage,
                skin,
                config.getString("saves.dir", DEFAULT_SAVE_DIR),
                TREE_EXTENSION,
                load
        );

        window.setWidth(config.getFloat("ui.saveload.width"));
        window.setHeight(config.getFloat("ui.saveload.height"));

        return window;
    }

    @Override
    public void render(float dt) {
        toGameView.convert(Gdx.input.getX(), Gdx.input.getY(), GAME_VIEW_COORDS);
        cell = world.findCell((int) GAME_VIEW_COORDS.x, (int) GAME_VIEW_COORDS.y);
        worldCameraParamsGetter.accept(WORLD_CAMERA_PARAMS);

        super.render(dt);
    }

    static class WorldCameraParams {
        final Vector3 position = new Vector3();
        float zoom;
    }
}
