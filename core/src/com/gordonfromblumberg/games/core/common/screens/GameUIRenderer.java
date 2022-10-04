package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.utils.CoordsConverter;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.evotree.model.Cell;
import com.gordonfromblumberg.games.core.evotree.model.Seed;
import com.gordonfromblumberg.games.core.evotree.model.TreePart;
import com.gordonfromblumberg.games.core.evotree.model.Wood;

public class GameUIRenderer extends UIRenderer {
    private Label lightLabel;
    private Label cellLabel;
    private GameWorld world;

    private final Vector3 GAME_VIEW_COORDS = new Vector3();
    private CoordsConverter toGameViewConverter;

    public GameUIRenderer(Viewport viewport, Stage stage, GameWorld world, CoordsConverter toGameViewConverter) {
        super(viewport, stage);

        this.world = world;
        this.toGameViewConverter = toGameViewConverter;
    }

    void setLightLabel(Label lightLabel) {
        this.lightLabel = lightLabel;
    }

    void setCellLabel(Label cellLabel) {
        this.cellLabel = cellLabel;
    }

    @Override
    public void render(float dt) {
        if (lightLabel != null) {
            toGameViewConverter.convert(Gdx.input.getX(), Gdx.input.getY(), GAME_VIEW_COORDS);
            Cell cell = world.findCell((int) GAME_VIEW_COORDS.x, (int) GAME_VIEW_COORDS.y);
            if (cell != null) {
                lightLabel.setText(cell.getSunLight() + (cell.isUnderSun() ? " / 1" : " / 0"));
                TreePart treePart = cell.getTreePart();
                if (treePart != null) {
                    if (treePart instanceof Seed) {
                        cellLabel.setText(treePart.getClass().getSimpleName() + " #" + ((Seed) treePart).getId());
                    } else if (treePart instanceof Wood) {
                        cellLabel.setText(treePart.getClass().getSimpleName() + "of tree #" + ((Wood) treePart).getTree().getId());
                    } else {
                        cellLabel.setText(treePart.getClass().getSimpleName());
                    }
                } else {
                    cellLabel.setText("No tree part");
                }
            } else {
                lightLabel.setText("No cell");
                cellLabel.setText("No tree part");
            }
        }

        super.render(dt);
    }
}
