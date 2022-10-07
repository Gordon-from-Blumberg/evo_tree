package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UIRenderer extends AbstractRenderer {
    protected Stage stage;

    public UIRenderer(Viewport viewport, Stage stage) {
        super(viewport);

        this.stage = stage;
        this.centerCamera = true;
    }

    @Override
    public void render(float dt) {
        stage.act();
        stage.draw();
    }
}
