package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

public class SpriteBatchRenderer extends AbstractRenderer {
    private static final Logger log = LogManager.create(SpriteBatchRenderer.class);
    protected SpriteBatch batch;

    public SpriteBatchRenderer(SpriteBatch batch) {
        super();

        log.info("SpriteBatchRenderer constructor for class " + getClass().getSimpleName());

        this.batch = batch;
    }

    @Override
    public void render(float dt) {

    }
}
