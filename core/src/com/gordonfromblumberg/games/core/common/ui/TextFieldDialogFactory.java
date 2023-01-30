package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import java.util.function.Consumer;

public class TextFieldDialogFactory {
    private final Stage stage;
    private final Skin skin;
    private String title;
    private String text;

    public TextFieldDialogFactory(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    public TextFieldDialogFactory title(String title) {
        this.title = title;
        return this;
    }

    public TextFieldDialogFactory text(String text) {
        this.text = text;
        return this;
    }

    public DialogExt create(String initialValue, Consumer<String> inputHandler) {
        final String textFieldName = this + inputHandler.toString();
        return new DialogBuilder(stage, title, skin)
                .text(text)
                .textField(initialValue, inputHandler, text, textFieldName, tf -> tf.setWidth(100f))
                .ok((Consumer<DialogExt>) dialog -> {
                    TextField field = dialog.getContentTable().findActor(textFieldName);
                    inputHandler.accept(field.getText());
                })
                .cancel(null)
                .build();
    }
}
