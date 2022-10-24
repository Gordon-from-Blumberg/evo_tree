package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpriteBatchRenderer extends AbstractRenderer {
    protected SpriteBatch batch;

    public SpriteBatchRenderer(SpriteBatch batch) {
        super();

        Gdx.app.log("INIT", "SpriteBatchRenderer constructor for class " + getClass().getSimpleName());

        this.batch = batch;
    }

    @Override
    public void render(float dt) {

    }
}
