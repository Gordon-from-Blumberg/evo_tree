package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.common.ui.SaveLoadWindow;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.GameWorldParams;
import com.gordonfromblumberg.games.core.evotree.model.ChangeLightByTime;
import com.gordonfromblumberg.games.core.evotree.model.ChangeLightByX;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class MainMenuScreen extends AbstractScreen {
    private static final Logger log = LogManager.create(MainMenuScreen.class);
    private static final String LAST_USED_CONFIG_KEY = "last-used-config";
    private static final String DEFAULT_CONFIG_SAVE_DIR = "saves/config";
    private static final String CONFIG_SAVE_EXTENSION = "conf.dat";

    TextButton textButton;
    final GameWorldParams worldParams = new GameWorldParams();
    private final Array<Consumer<GameWorldParams>> updateListeners = new Array<>();

    public MainMenuScreen(SpriteBatch batch) {
        super(batch);

        color = Color.FOREST;
    }

    @Override
    protected void createUiRenderer() {
        super.createUiRenderer();

        loadDefaults();
        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);
        final Table rootTable = uiRenderer.rootTable;

        final float fieldWidth = 50f;
        rootTable.add(createSizeTable(uiSkin, fieldWidth)).left();

        rootTable.row();
        rootTable.add(createSunTable(uiSkin, fieldWidth)).left();

        rootTable.row();
        rootTable.add(createLightDistribution(uiSkin)).left();

        rootTable.row();
        rootTable.add(createSaveLoadButtons(uiSkin));

        rootTable.row();
        textButton = new TextButton("PLAY", uiSkin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Preferences prefs = Gdx.app.getPreferences(LAST_USED_CONFIG_KEY);
                worldParams.save(prefs);
                prefs.putBoolean("exists", true);
                prefs.flush();
                Main.getInstance().setScreen(new GameScreen(batch, worldParams));
            }
        });
        rootTable.add(textButton);
    }

    private Table createSizeTable(Skin skin, float fieldWidth) {
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
        widthField.setValue(worldParams.getWidth());
        table.add(widthField)
                .left();
        table.add(new Label("Width", skin))
                .left();
        updateListeners.add(params -> widthField.setValue(params.getWidth()));

        table.row();
        IntChangeableLabel heightField = new IntChangeableLabel(skin, worldParams::setHeight);
        heightField.setMinValue(30);
        heightField.setMaxValue(100);
        heightField.setFieldWidth(fieldWidth);
        heightField.setFieldDisabled(false);
        heightField.setStep(5);
        heightField.setValue(worldParams.getHeight());
        table.add(heightField)
                .left();
        table.add(new Label("Height", skin))
                .left();
        updateListeners.add(params -> heightField.setValue(params.getHeight()));

        return table;
    }

    private Table createSunTable(Skin skin, float fieldWidth) {
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
        sunLightField.setValue(worldParams.getSunLight());
        table.add(sunLightField)
                .left();
        table.add(new Label("Sun light", skin))
                .left();
        updateListeners.add(params -> sunLightField.setValue(params.getSunLight()));

//        table.row();
//        IntChangeableLabel lightAbsorptionField = new IntChangeableLabel(skin, worldParams::setLightAbsorptionStep);
//        lightAbsorptionField.setMinValue(1);
//        lightAbsorptionField.setMaxValue(10);
//        lightAbsorptionField.setFieldWidth(fieldWidth);
//        lightAbsorptionField.setFieldDisabled(false);
//        lightAbsorptionField.setStep(1);
//        lightAbsorptionField.setValue(worldParams.getLightAbsorptionStep());
//        table.add(lightAbsorptionField)
//                .left();
//        table.add(new Label("Light absorption step", skin))
//                .left();
//        updateListeners.add(params -> lightAbsorptionField.setValue(params.getLightAbsorptionStep()));

        return table;
    }

    private Table createLightDistribution(Skin skin) {
        Table table = UIUtils.createTable();
        final float indent = 20f;
        final float fieldWidth = 50f;

        ChangeLightByTime.register();
        CheckBox byTimeCheckBox = new CheckBox("Change by time", skin);
        byTimeCheckBox.setChecked(worldParams.isSelected(ChangeLightByTime.class.getSimpleName()));
        table.add(byTimeCheckBox).colspan(2).left();
        updateListeners.add(params -> byTimeCheckBox.setChecked(params.isSelected(ChangeLightByTime.class.getSimpleName())));

        table.row();
        IntChangeableLabel byTimeMaxField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.max", v));
        byTimeMaxField.setMinValue(0);
        byTimeMaxField.setMaxValue(50);
        byTimeMaxField.setFieldWidth(fieldWidth);
        byTimeMaxField.setFieldDisabled(false);
        byTimeMaxField.setStep(1);
        byTimeMaxField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.max"));
        table.add(byTimeMaxField).padLeft(indent).left();
        table.add(new Label("Max", skin)).left();
        updateListeners.add(params -> byTimeMaxField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.max")));

        table.row();
        IntChangeableLabel byTimeMinField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.min", v));
        byTimeMinField.setMinValue(-50);
        byTimeMinField.setMaxValue(0);
        byTimeMinField.setFieldWidth(fieldWidth);
        byTimeMinField.setFieldDisabled(false);
        byTimeMinField.setStep(1);
        byTimeMinField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.min"));
        table.add(byTimeMinField).padLeft(indent).left();
        table.add(new Label("Min", skin)).left();
        updateListeners.add(params -> byTimeMinField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.min")));

        table.row();
        IntChangeableLabel byTimeDelayField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.delay", v));
        byTimeDelayField.setMinValue(50);
        byTimeDelayField.setMaxValue(5000);
        byTimeDelayField.setFieldWidth(fieldWidth);
        byTimeDelayField.setFieldDisabled(false);
        byTimeDelayField.setStep(50);
        byTimeDelayField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.delay"));
        table.add(byTimeDelayField).padLeft(indent).left();
        table.add(new Label("Delay", skin)).left();
        updateListeners.add(params -> byTimeDelayField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.delay")));

        table.row();
        IntChangeableLabel byTimeStepField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByTime.step", v));
        byTimeStepField.setMinValue(1);
        byTimeStepField.setMaxValue(10);
        byTimeStepField.setFieldWidth(fieldWidth);
        byTimeStepField.setFieldDisabled(false);
        byTimeStepField.setStep(1);
        byTimeStepField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.step"));
        table.add(byTimeStepField).padLeft(indent).left();
        table.add(new Label("Step", skin)).left();
        updateListeners.add(params -> byTimeStepField.setValue((int) worldParams.getDecoratorParam("ChangeLightByTime.step")));

        byTimeCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) event.getListenerActor()).isChecked())
                    worldParams.addDecorator(ChangeLightByTime.class.getSimpleName());
                else
                    worldParams.removeDecorator(ChangeLightByTime.class.getSimpleName());
            }
        });

        ChangeLightByX.register();
        table.row();
        CheckBox byXCheckBox = new CheckBox("Change by X axis", skin);
        byXCheckBox.setChecked(worldParams.isSelected(ChangeLightByX.class.getSimpleName()));
        table.add(byXCheckBox).colspan(2).left();
        updateListeners.add(params -> byXCheckBox.setChecked(params.isSelected(ChangeLightByX.class.getSimpleName())));

        table.row();
        IntChangeableLabel byXHalfMagnitudeField = new IntChangeableLabel(skin,
                v -> worldParams.setDecoratorParam("ChangeLightByX.halfMagnitude", v));
        byXHalfMagnitudeField.setMinValue(1);
        byXHalfMagnitudeField.setMaxValue(50);
        byXHalfMagnitudeField.setFieldWidth(fieldWidth);
        byXHalfMagnitudeField.setFieldDisabled(false);
        byXHalfMagnitudeField.setStep(1);
        byXHalfMagnitudeField.setValue((int) worldParams.getDecoratorParam("ChangeLightByX.halfMagnitude"));
        table.add(byXHalfMagnitudeField).padLeft(indent).left();
        table.add(new Label("Half magnitude", skin)).left();
        updateListeners.add(params -> byXHalfMagnitudeField.setValue((int) worldParams.getDecoratorParam("ChangeLightByX.halfMagnitude")));

        byXCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) event.getListenerActor()).isChecked())
                    worldParams.addDecorator(ChangeLightByX.class.getSimpleName());
                else
                    worldParams.removeDecorator(ChangeLightByX.class.getSimpleName());
            }
        });
        return table;
    }

    private Table createSaveLoadButtons(Skin skin) {
        Table table = UIUtils.createTable();
        TextButton saveButton = new TextButton("SAVE", skin);
        table.add(saveButton);
        saveButton.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSaveLoadWindow(false, skin);
            }
        });
        TextButton loadButton = new TextButton("LOAD", skin);
        table.add(loadButton);
        loadButton.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSaveLoadWindow(true, skin);
            }
        });
        return table;
    }

    private void showSaveLoadWindow(boolean load, Skin skin) {
        ConfigManager config = AbstractFactory.getInstance().configManager();
        SaveLoadWindow window = new SaveLoadWindow(
                uiRenderer.stage,
                skin,
                config.getString("saves.config.dir", DEFAULT_CONFIG_SAVE_DIR),
                CONFIG_SAVE_EXTENSION,
                load
        );

        window.setWidth(config.getFloat("ui.saveload.width"));
        window.setHeight(config.getFloat("ui.saveload.height"));

        window.open(load ? this::loadAndUpdateView : worldParams::save);
    }

    private void loadAndUpdateView(ByteBuffer bb) {
        worldParams.load(bb);
        for (Consumer<GameWorldParams> updater : updateListeners) {
            updater.accept(worldParams);
        }
    }

    private void loadDefaults() {
        Preferences lastUsedPrefs = Gdx.app.getPreferences(LAST_USED_CONFIG_KEY);
        if (lastUsedPrefs.getBoolean("exists")) {
            log.debug("Load config from preferences");
            worldParams.load(lastUsedPrefs);
        } else {
            log.debug("Load config from config");
            worldParams.load(AbstractFactory.getInstance().configManager());
        }
    }
}
