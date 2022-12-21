package com.gordonfromblumberg.games.core.common.screens;

import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

public class FBORenderer extends AbstractRenderer {
    private static final Logger log = LogManager.create(FBORenderer.class);

    public FBORenderer() {
        log.info("FBORenderer constructor for class " + getClass().getSimpleName());
    }

    @Override
    public void render(float dt) {

    }
}
