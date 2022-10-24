package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;

public class UIRenderer extends SpriteBatchRenderer {
    protected Stage stage;
    protected Table rootTable;

    public UIRenderer(SpriteBatch batch) {
        super(batch);

        Gdx.app.log("INIT", "UIRenderer constructor for class " + getClass().getSimpleName());

        this.centerCamera = true;
        this.stage = createStage();
        if (Main.DEBUG_UI) {
            stage.setDebugAll(true);
        }

        Table table = UIUtils.createTable();
        table.setFillParent(true);
        this.rootTable = table;
        this.stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float dt) {
        stage.act();
        stage.draw();
    }

    protected Stage createStage() {
        return new Stage(viewport, batch);
    }

    public void addListener(InputListener listener) {
        stage.addListener(listener);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
