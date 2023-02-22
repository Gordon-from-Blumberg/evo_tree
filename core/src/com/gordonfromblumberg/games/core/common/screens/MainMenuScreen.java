package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorldParams;

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

        rootTable.add(createSizeTable(uiSkin, config));

        rootTable.row();
        rootTable.add(createSunTable(uiSkin, config));

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

    private Table createSizeTable(Skin skin, ConfigManager config) {
        Table table = UIUtils.createTable();
        table.add(new Label("World size", skin))
                .center().colspan(2);

        table.row();
        table.add(new Label("Width", skin))
                .right();
        IntChangeableLabel widthField = new IntChangeableLabel(skin, worldParams::setWidth);
        widthField.setMinValue(100);
        widthField.setMaxValue(1000);
        widthField.setFieldWidth(50);
        widthField.setFieldDisabled(false);
        widthField.setStep(50);
        widthField.setValue(config.getInteger("world.width"));
        table.add(widthField)
                .left();

        table.row();
        table.add(new Label("Height", skin))
                .right();
        IntChangeableLabel heightField = new IntChangeableLabel(skin, worldParams::setHeight);
        heightField.setMinValue(30);
        heightField.setMaxValue(100);
        heightField.setFieldWidth(40);
        heightField.setFieldDisabled(false);
        heightField.setStep(5);
        heightField.setValue(config.getInteger("world.height"));
        table.add(heightField)
                .left();

        return table;
    }

    private Table createSunTable(Skin skin, ConfigManager config) {
        Table table = UIUtils.createTable();
        table.add(new Label("Sun light distribution", skin))
                .center().colspan(2);

        table.row();
        table.add(new Label("Sun light", skin))
                .right();
        IntChangeableLabel sunLightField = new IntChangeableLabel(skin, worldParams::setSunLight);
        sunLightField.setMinValue(25);
        sunLightField.setMaxValue(100);
        sunLightField.setFieldWidth(40);
        sunLightField.setFieldDisabled(false);
        sunLightField.setStep(5);
        sunLightField.setValue(config.getInteger("world.sunLight"));
        table.add(sunLightField)
                .left();

        table.row();
        table.add(new Label("Light absorption step", skin))
                .right();
        IntChangeableLabel lightAbsorptionField = new IntChangeableLabel(skin, worldParams::setLightAbsorptionStep);
        lightAbsorptionField.setMinValue(1);
        lightAbsorptionField.setMaxValue(10);
        lightAbsorptionField.setFieldWidth(30);
        lightAbsorptionField.setFieldDisabled(false);
        lightAbsorptionField.setStep(1);
        lightAbsorptionField.setValue(config.getInteger("world.lightAbsorptionStep"));
        table.add(lightAbsorptionField)
                .left();
        return table;
    }
}
