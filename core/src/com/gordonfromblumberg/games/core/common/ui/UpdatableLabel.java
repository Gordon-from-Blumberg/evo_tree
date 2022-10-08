package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import java.util.function.Supplier;

public class UpdatableLabel extends Label {
    private final Supplier<Object> textSupplier;

    public UpdatableLabel(Skin skin, Supplier<Object> textSupplier) {
        super(null, skin);

        this.textSupplier = textSupplier;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        setText(String.valueOf(textSupplier.get()));
    }

    public UpdatableLabel center() {
        setAlignment(Align.center);
        return this;
    }
}
