package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.CoordsConverter;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.evotree.model.Seed;
import com.gordonfromblumberg.games.core.evotree.model.TreePart;
import com.gordonfromblumberg.games.core.evotree.model.Wood;

public class GameUIRenderer extends UIRenderer {
    private Label turnsLabel;
    private Label worldLightLabel;
    private Label seedsLabel;
    private Label treesLabel;

    private Label lightLabel;
    private Label cellLabel;
    private Label treeLabel;
    private GameWorld world;

    private final Vector3 GAME_VIEW_COORDS = new Vector3();
    private CoordsConverter toGameViewConverter;

    public GameUIRenderer(Viewport viewport, Stage stage, GameWorld world, CoordsConverter toGameViewConverter) {
        super(viewport, stage);

        Gdx.app.log("INIT", "GameUIRenderer constructor");

        this.world = world;
        this.toGameViewConverter = toGameViewConverter;
    }

    public Table createInfoTable(Skin uiSkin) {
        Table table = new Table();
        table.add(turnsLabel = new Label("Turn 1", uiSkin));

        table.row();
        table.add(worldLightLabel = new Label("Light " + world.getSunLight(), uiSkin));

        table.row();
        table.add(seedsLabel = new Label("Seeds " + world.getSeedCount(), uiSkin));

        table.row();
        table.add(treesLabel = new Label("Trees " + world.getTreeCount(), uiSkin));

        if (Main.DEBUG_UI) {
            table.debugAll();
        }

        return table;
    }

    void setLightLabel(Label lightLabel) {
        this.lightLabel = lightLabel;
    }

    void setCellLabel(Label cellLabel) {
        this.cellLabel = cellLabel;
    }

    void setTreeLabel(Label treeLabel) {
        this.treeLabel = treeLabel;
    }

    @Override
    public void render(float dt) {
        turnsLabel.setText("Turn " + world.getTurn());
        if (lightLabel != null) {
            toGameViewConverter.convert(Gdx.input.getX(), Gdx.input.getY(), GAME_VIEW_COORDS);
            Cell cell = world.findCell((int) GAME_VIEW_COORDS.x, (int) GAME_VIEW_COORDS.y);
            if (cell != null) {
                cellLabel.setText(cell.getX() + ", " + cell.getY());
                lightLabel.setText(cell.getSunLight() + (cell.isUnderSun() ? " / 1" : " / 0"));
                TreePart treePart = cell.getTreePart();
                if (treePart != null) {
                    if (treePart instanceof Seed) {
                        treeLabel.setText(treePart.getClass().getSimpleName() + " #" + ((Seed) treePart).getId());
                    } else if (treePart instanceof Wood) {
                        treeLabel.setText(treePart.getClass().getSimpleName() + " of tree #" + ((Wood) treePart).getTree().getId());
                    } else {
                        treeLabel.setText(treePart.getClass().getSimpleName());
                    }
                } else {
                    treeLabel.setText("No tree");
                }
            } else {
                cellLabel.setText("No cell");
                lightLabel.setText("No cell");
                treeLabel.setText("No tree");
            }
        }

        super.render(dt);
    }
}
