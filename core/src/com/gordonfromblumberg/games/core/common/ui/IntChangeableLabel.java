package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.function.IntConsumer;

public class IntChangeableLabel extends HorizontalGroup {
    private final Button prev;
    private final Button next;
    private final Label valueLabel;
    private final IntConsumer onChangeListener;

    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;
    private int value;
    private int step = 1;
    private ChangeFunction changeFunction = ChangeFunction.linear;

    public IntChangeableLabel(Skin skin, IntConsumer onChangeListener) {
        this.prev = new TextButton("<", skin);
        this.next = new TextButton(">", skin);
        this.valueLabel = new Label(String.valueOf(value), skin);
        this.onChangeListener = onChangeListener;

        addClickListeners();
        addActor(prev);
        addActor(valueLabel);
        addActor(next);
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setValue(int value) {
        this.value = value;
        checkValue();
        valueLabel.setText(this.value);
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void linear() {
        changeFunction = ChangeFunction.linear;
    }

    public void geometric() {
        changeFunction = ChangeFunction.geometric;
    }

    private void checkValue() {
        if (value < minValue) value = minValue;
        else if (value > maxValue) value = maxValue;
    }

    private void addClickListeners() {
        ClickListener clickListener = new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int v = value;
                switch (changeFunction) {
                    case linear:
                        if (event.getListenerActor() == prev) {
                            v -= step;
                        } else if (event.getListenerActor() == next) {
                            v += step;
                        }
                        break;
                    case geometric:
                        if (event.getListenerActor() == prev) {
                            v /= step;
                        } else if (event.getListenerActor() == next) {
                            v *= step;
                        }
                        break;
                }

                value = v;
                checkValue();
                valueLabel.setText(value);
                onChangeListener.accept(value);
            }
        };
        prev.addListener(clickListener);
        next.addListener(clickListener);
    }

    private enum ChangeFunction {
        linear,
        geometric;
    }
}
