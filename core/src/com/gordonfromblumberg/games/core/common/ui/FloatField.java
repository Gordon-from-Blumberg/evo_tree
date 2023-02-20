package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.gordonfromblumberg.games.core.common.utils.FloatConsumer;

public class FloatField extends TextField {
    private Skin skin;
    private float value;
    float minValue, maxValue;
    private FloatConsumer handler;

    private FloatField(Skin skin, float value, float minValue, float maxValue, FloatConsumer handler) {
        super(String.valueOf(value), skin);

        if (minValue > maxValue)
            throw new IllegalArgumentException("Min " + minValue + " can not be greater than max " + maxValue);

        this.skin = skin;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.handler = handler;

        setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                try {
                    float newValue = Float.parseFloat(textField.getText());
                    if (!checkValue(newValue)) {
                        trySetError(textField);
                        return;
                    }

                    setValueInternal(newValue);
                    textField.setStyle(skin.get(TextFieldStyle.class));
                } catch (NumberFormatException e) {
                    trySetError(textField);
                }
            }

            private void trySetError(TextField textField) {
                textField.setStyle(skin.get("error", TextFieldStyle.class));
            }
        });

        setTextFieldFilter((textField, c) -> Character.isDigit(c)
                || c == '.' || c == ','
                || textField.getCursorPosition() == 0 && c == '-');
    }

    public static FloatFieldBuilder builder() {
        return new FloatFieldBuilder();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (!checkValue(value)) {
            throw new IllegalArgumentException("Value " + value + " is out of range [" + minValue + ", " + maxValue + "]");
        }
        setValueInternal(value);
    }

    @Override
    public float getPrefWidth() {
        return getWidth();
    }

    public static class FloatFieldBuilder {
        private Skin skin;
        private float value;
        private float minValue = -Float.MAX_VALUE, maxValue = Float.MAX_VALUE;
        private FloatConsumer handler;

        public FloatFieldBuilder value(float value) {
            this.value = value;
            return this;
        }

        public FloatFieldBuilder skin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public FloatFieldBuilder minValue(float minValue) {
            this.minValue = minValue;
            return this;
        }

        public FloatFieldBuilder maxValue(float maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public FloatFieldBuilder handler(FloatConsumer handler) {
            this.handler = handler;
            return this;
        }

        public FloatField build() {
            return new FloatField(skin, value, minValue, maxValue, handler);
        }
    }

    private boolean checkValue(float value) {
        return minValue <= value && value <= maxValue;
    }

    private void setValueInternal(float value) {
        this.value = value;
        setText(String.valueOf(value));
        handler.accept(value);
    }
}
