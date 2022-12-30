package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

public class UIWindow extends Window {
    private static final Logger log = LogManager.create(UIWindow.class);
    private final TextButton closeButton;
    private Runnable onOpenHandler, onCloseHandler;

    public UIWindow(String title, Skin skin) {
        super(title, skin);

        closeButton = new TextButton("x", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        getTitleTable().add(closeButton).height(getTitleLabel().getHeight());
        if (Main.DEBUG_UI)
            getTitleTable().debugAll();
    }

    public void toScreenCenter() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        setPosition((screenWidth - getWidth()) / 2, (screenHeight - getHeight()) / 2);
    }

    public void setOnOpenHandler(Runnable onOpenHandler) {
        this.onOpenHandler = onOpenHandler;
    }

    public void setOnCloseHandler(Runnable onCloseHandler) {
        this.onCloseHandler = onCloseHandler;
    }

    public void open() {
        setVisible(true);
        if (onOpenHandler != null)
            onOpenHandler.run();
    }

    public void close() {
        setVisible(false);
        if (onCloseHandler != null)
            onCloseHandler.run();
    }

    public void toggle() {
        if (isVisible())
            close();
        else
            open();
    }
}
