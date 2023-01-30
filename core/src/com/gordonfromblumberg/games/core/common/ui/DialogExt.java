package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

import java.util.function.Consumer;

public class DialogExt extends Dialog {
    private static final Logger log = LogManager.create(DialogExt.class);

    private final Stage stage;
    private final TextButton closeButton;

    public DialogExt(Stage stage, String title, Skin skin) {
        super(title, skin);

        this.stage = stage;
        this.closeButton = new TextButton("x", skin);
        this.closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        getTitleTable().add(closeButton).height(getTitleLabel().getHeight());

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE && isVisible()) {
                    hide();
                    return true;
                }
                return false;
            }
        });

        if (Main.DEBUG_UI)
            getTitleTable().debugAll();
    }

    public void toScreenCenter() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        setPosition((screenWidth - getWidth()) / 2, (screenHeight - getHeight()) / 2);
    }

    public void open() {
        setVisible(true);
        show(stage);
    }

    @Override
    public void hide() {
        super.hide();
        setVisible(false);
    }

    public void toggle() {
        if (isVisible())
            hide();
        else
            open();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void result(Object object) {
        if (object instanceof Consumer)
            ((Consumer<DialogExt>) object).accept(this);
        else if (object instanceof Runnable)
            ((Runnable) object).run();
    }
}
