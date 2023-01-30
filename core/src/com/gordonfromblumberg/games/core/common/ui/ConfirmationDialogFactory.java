package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ConfirmationDialogFactory {
    private final Stage stage;
    private final Skin skin;
    private String title;
    private String text;

    public ConfirmationDialogFactory(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    public ConfirmationDialogFactory title(String title) {
        this.title = title;
        return this;
    }

    public ConfirmationDialogFactory text(String text) {
        this.text = text;
        return this;
    }

    public DialogExt create(Runnable okHandler) {
        return new DialogBuilder(stage, title, skin)
                .text(text)
                .ok(okHandler)
                .cancel(null)
                .build();
    }
}
