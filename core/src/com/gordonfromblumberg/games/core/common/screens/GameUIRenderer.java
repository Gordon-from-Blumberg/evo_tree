package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.utils.CoordsConverter;
import com.gordonfromblumberg.games.core.common.world.GameWorld;
import com.gordonfromblumberg.games.core.evotree.model.Cell;

public class GameUIRenderer extends UIRenderer {
    private Label lightLabel;
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

    @Override
    public void render(float dt) {
        if (lightLabel != null) {
            toGameViewConverter.convert(Gdx.input.getX(), Gdx.input.getY(), GAME_VIEW_COORDS);
            Cell cell = world.findCell((int) GAME_VIEW_COORDS.x, (int) GAME_VIEW_COORDS.y);
            if (cell != null) {
                lightLabel.setText(cell.getSunLight() + (cell.isUnderSun() ? " / 1" : " / 0"));
            } else {
                lightLabel.setText("No cell");
            }
        }

        super.render(dt);
    }
}
