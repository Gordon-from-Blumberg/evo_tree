package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.ui.UpdatableLabel;
import com.gordonfromblumberg.games.core.common.utils.CoordsConverter;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.evotree.event.SelectTreeEvent;
import com.gordonfromblumberg.games.core.evotree.model.*;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.evotree.model.Tree;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.padLeft;

public class GameUIRenderer extends UIRenderer {
    private GameWorld world;

    private final Vector3 GAME_VIEW_COORDS = new Vector3();
    private CoordsConverter toGameViewConverter;
    private Cell cell;

    public GameUIRenderer(Viewport viewport, Stage stage, GameWorld world, CoordsConverter toGameViewConverter) {
        super(viewport, stage);

        Gdx.app.log("INIT", "GameUIRenderer constructor");

        this.world = world;
        this.toGameViewConverter = toGameViewConverter;
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
        table.add(new UpdatableLabel(uiSkin, () -> cell != null && cell.getTreePart() != null
                ? cell.getTreePart().getLightAbsorption() : "No tree part")
        )
                .left();

        table.row();
        table.add(new Label("Energy", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> cell != null && cell.getTreePart() != null
                ? cell.getTreePart().calcEnergy(world.getGrid()) : "No tree part")
        )
                .left();

        table.row();
        table.add(new Label("Tree", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
                    TreePart treePart = cell != null ? cell.getTreePart() : null;
                    if (treePart instanceof Seed) {
                        return "Seed #" + ((Seed) treePart).getId();
                    }
                    if (treePart instanceof Wood) {
                        return "#" + ((Wood) treePart).getTree().getId() + ", " + treePart.getClass().getSimpleName();
                    }
                    return treePart != null ? treePart.getClass().getSimpleName() : "No tree";
                }))
                .minWidth(160);

        table.row();
        table.add(new Label("Tree energy", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
            TreePart treePart = cell != null ? cell.getTreePart() : null;
            if (treePart instanceof Wood) {
                return  ((Wood) treePart).getTree().getEnergy();
            }
            return "No tree";
        }))
                .left();

        table.row();
        table.add(new Label("Tree size", uiSkin))
                .padRight(pad).right();
        table.add(new UpdatableLabel(uiSkin, () -> {
                    TreePart treePart = cell != null ? cell.getTreePart() : null;
                    if (treePart instanceof Wood) {
                        return  ((Wood) treePart).getTree().getSize();
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
        speedControl.setMinValue(5);
        speedControl.setMaxValue(50);
        speedControl.setStep(5);
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
                                .append(padLeft(gene.getValue(Gene.MOVE_TO), 2))
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

    @Override
    public void render(float dt) {
        toGameViewConverter.convert(Gdx.input.getX(), Gdx.input.getY(), GAME_VIEW_COORDS);
        cell = world.findCell((int) GAME_VIEW_COORDS.x, (int) GAME_VIEW_COORDS.y);

        super.render(dt);
    }
}
