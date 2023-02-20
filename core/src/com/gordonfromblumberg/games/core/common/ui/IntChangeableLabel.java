package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.function.IntConsumer;

public class IntChangeableLabel extends HorizontalGroup {
    private final Button prev;
    private final Button next;
    private final IntField valueField;

    private int step = 1;
    private ChangeFunction changeFunction = ChangeFunction.linear;

    public IntChangeableLabel(Skin skin, IntConsumer onChangeListener) {
        this.prev = new TextButton("<", skin);
        this.next = new TextButton(">", skin);
        this.valueField = IntField.builder()
                .skin(skin)
                .value(0)
                .handler(onChangeListener)
                .build();
        this.valueField.setDisabled(true);

        addClickListeners();
        addActor(prev);
        addActor(valueField);
        addActor(next);
    }

    public void setMinValue(int minValue) {
        valueField.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        valueField.maxValue = maxValue;
    }

    public void setValue(int value) {
        valueField.setValue(value);
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

    public void setFieldWidth(float width) {
        valueField.setWidth(width);
    }

    private void addClickListeners() {
        ClickListener clickListener = new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int v = valueField.getValue();
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

                try {
                    valueField.setValue(v);
                } catch (IllegalArgumentException e) {
                    //do nothing
                }
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
