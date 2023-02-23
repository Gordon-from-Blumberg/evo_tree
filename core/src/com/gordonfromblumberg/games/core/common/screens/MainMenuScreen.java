package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorldParams;
import com.gordonfromblumberg.games.core.evotree.model.ChangeLightByTime;
import com.gordonfromblumberg.games.core.evotree.model.ChangeLightByX;

public class MainMenuScreen extends AbstractScreen {
    private static final Logger log = LogManager.create(MainMenuScreen.class);

    TextButton textButton;
    final GameWorldParams worldParams = new GameWorldParams();

    public MainMenuScreen(SpriteBatch batch) {
        super(batch);

        color = Color.FOREST;

        log.debug("Local storage path = " + Gdx.files.getLocalStoragePath());
        log.debug("External storage path = " + Gdx.files.getExternalStoragePath());
    }

    @Override
    protected void createUiRenderer() {
        super.createUiRenderer();

        final ConfigManager config = AbstractFactory.getInstance().configManager();
        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);
        final Table rootTable = uiRenderer.rootTable;

        final float fieldWidth = 50f;
        rootTable.add(createSizeTable(uiSkin, config, fieldWidth)).left();

        rootTable.row();
        rootTable.add(createSunTable(uiSkin, config, fieldWidth)).left();

        rootTable.row();
        rootTable.add(createLightDistribution(uiSkin)).left();

        rootTable.row();
        textButton = new TextButton("PLAY", uiSkin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.getInstance().setScreen(new GameScreen(batch, worldParams));
            }
        });
        rootTable.add(textButton);
    }

    private Table createSizeTable(Skin skin, ConfigManager config, float fieldWidth) {
        Table table = UIUtils.createTable();
        table.add(new Label("World size", skin))
                .center().colspan(2);

        table.row();
        IntChangeableLabel widthField = new IntChangeableLabel(skin, worldParams::setWidth);
        widthField.setMinValue(100);
        widthField.setMaxValue(1000);
        widthField.setFieldWidth(fieldWidth);
        widthField.setFieldDisabled(false);
        widthField.setStep(50);
        widthField.setValue(config.getInteger("world.width"));
        table.add(widthField)
                .left();
        table.add(new Label("Width", skin))
                .left();

        table.row();
        IntChangeableLabel heightField = new IntChangeableLabel(skin, worldParams::setHeight);
        heightField.setMinValue(30);
        heightField.setMaxValue(100);
        heightField.setFieldWidth(fieldWidth);
        heightField.setFieldDisabled(false);
        heightField.setStep(5);
        heightField.setValue(config.getInteger("world.height"));
        table.add(heightField)
                .left();
        table.add(new Label("Height", skin))
                .left();

        return table;
    }

    private Table createSunTable(Skin skin, ConfigManager config, float fieldWidth) {
        Table table = UIUtils.createTable();
        table.add(new Label("Sun light distribution", skin))
                .center().colspan(2);

        table.row();
        IntChangeableLabel sunLightField = new IntChangeableLabel(skin, worldParams::setSunLight);
        sunLightField.setMinValue(25);
        sunLightField.setMaxValue(100);
        sunLightField.setFieldWidth(fieldWidth);
        sunLightField.setFieldDisabled(false);
        sunLightField.setStep(5);
        sunLightField.setValue(config.getInteger("world.sunLight"));
        table.add(sunLightField)
                .left();
        table.add(new Label("Sun light", skin))
                .left();

        table.row();
        IntChangeableLabel lightAbsorptionField = new IntChangeableLabel(skin, worldParams::setLightAbsorptionStep);
        lightAbsorptionField.setMinValue(1);
        lightAbsorptionField.setMaxValue(10);
        lightAbsorptionField.setFieldWidth(fieldWidth);
        lightAbsorptionField.setFieldDisabled(false);
        lightAbsorptionField.setStep(1);
        lightAbsorptionField.setValue(config.getInteger("world.lightAbsorptionStep"));
        table.add(lightAbsorptionField)
                .left();
        table.add(new Label("Light absorption step", skin))
                .left();
        return table;
    }

    private Table createLightDistribution(Skin skin) {
        Table table = UIUtils.createTable();
        CheckBox byTimeCheckBox = new CheckBox("Change by time", skin);
        table.add(byTimeCheckBox).colspan(2);

        final float indent = 20f;
        final float fieldWidth = 50f;
        table.row();
        IntChangeableLabel byTimeMaxField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.max", v));
        byTimeMaxField.setMinValue(1);
        byTimeMaxField.setMaxValue(50);
        byTimeMaxField.setFieldWidth(fieldWidth);
        byTimeMaxField.setFieldDisabled(false);
        byTimeMaxField.setStep(1);
        byTimeMaxField.setValue(15);
        table.add(byTimeMaxField).padLeft(indent).left();
        table.add(new Label("Max", skin)).left();

        table.row();
        IntChangeableLabel byTimeMinField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.min", v));
        byTimeMinField.setMinValue(-50);
        byTimeMinField.setMaxValue(-1);
        byTimeMinField.setFieldWidth(fieldWidth);
        byTimeMinField.setFieldDisabled(false);
        byTimeMinField.setStep(1);
        byTimeMinField.setValue(-15);
        table.add(byTimeMinField).padLeft(indent).left();
        table.add(new Label("Min", skin)).left();

        table.row();
        IntChangeableLabel byTimeDelayField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.delay", v));
        byTimeDelayField.setMinValue(50);
        byTimeDelayField.setMaxValue(5000);
        byTimeDelayField.setFieldWidth(fieldWidth);
        byTimeDelayField.setFieldDisabled(false);
        byTimeDelayField.setStep(50);
        byTimeDelayField.setValue(500);
        table.add(byTimeDelayField).padLeft(indent).left();
        table.add(new Label("Delay", skin)).left();

        table.row();
        IntChangeableLabel byTimeStepField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.step", v));
        byTimeStepField.setMinValue(1);
        byTimeStepField.setMaxValue(10);
        byTimeStepField.setFieldWidth(fieldWidth);
        byTimeStepField.setFieldDisabled(false);
        byTimeStepField.setStep(1);
        byTimeStepField.setValue(1);
        table.add(byTimeStepField).padLeft(indent).left();
        table.add(new Label("Step", skin)).left();

        byTimeCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) event.getListenerActor()).isChecked())
                    worldParams.addDecorator(ChangeLightByTime.PRODUCER);
                else
                    worldParams.removeDecorator(ChangeLightByTime.PRODUCER);
            }
        });

        table.row();
        CheckBox byXCheckBox = new CheckBox("Change by X axis", skin);
        table.add(byXCheckBox).colspan(2);

        table.row();
        IntChangeableLabel byXHalfMagnitudeField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByX.halfMagnitude", v));
        byXHalfMagnitudeField.setMinValue(1);
        byXHalfMagnitudeField.setMaxValue(50);
        byXHalfMagnitudeField.setFieldWidth(fieldWidth);
        byXHalfMagnitudeField.setFieldDisabled(false);
        byXHalfMagnitudeField.setStep(1);
        byXHalfMagnitudeField.setValue(15);
        table.add(byXHalfMagnitudeField).padLeft(indent).left();
        table.add(new Label("Half magnitude", skin)).left();

        byXCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) event.getListenerActor()).isChecked())
                    worldParams.addDecorator(ChangeLightByX.PRODUCER);
                else
                    worldParams.removeDecorator(ChangeLightByX.PRODUCER);
            }
        });
        return table;
    }
}
