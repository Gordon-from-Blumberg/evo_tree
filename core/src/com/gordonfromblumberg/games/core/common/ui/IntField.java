package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.function.IntConsumer;

public class IntField extends TextField {
    private Skin skin;
    private int value;
    int minValue, maxValue;
    private IntConsumer handler;

    private IntField(Skin skin, int value, int minValue, int maxValue, IntConsumer handler) {
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
                    int newValue = Integer.parseInt(textField.getText());
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

        setTextFieldFilter((textField, c) -> Character.isDigit(c) || textField.getCursorPosition() == 0 && c == '-');
    }

    public static IntField.IntFieldBuilder builder() {
        return new IntField.IntFieldBuilder();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (!checkValue(value)) {
            throw new IllegalArgumentException("Value " + value + " is out of range [" + minValue + ", " + maxValue + "]");
        }
        setValueInternal(value);
    }

    @Override
    public float getPrefWidth() {
        return getWidth();
    }

    public static class IntFieldBuilder {
        private Skin skin;
        private int value;
        private int minValue = Integer.MIN_VALUE, maxValue = Integer.MAX_VALUE;
        private IntConsumer handler;

        public IntFieldBuilder value(int value) {
            this.value = value;
            return this;
        }

        public IntFieldBuilder skin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public IntFieldBuilder minValue(int minValue) {
            this.minValue = minValue;
            return this;
        }

        public IntFieldBuilder maxValue(int maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public IntFieldBuilder handler(IntConsumer handler) {
            this.handler = handler;
            return this;
        }

        public IntField build() {
            return new IntField(skin, value, minValue, maxValue, handler);
        }
    }

    private boolean checkValue(int value) {
        return minValue <= value && value <= maxValue;
    }

    private void setValueInternal(int value) {
        this.value = value;
        setText(String.valueOf(value));
        handler.accept(value);
    }
}
