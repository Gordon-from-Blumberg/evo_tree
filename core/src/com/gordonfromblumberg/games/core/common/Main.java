package com.gordonfromblumberg.games.core.common;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.screens.AbstractScreen;
import com.gordonfromblumberg.games.core.common.screens.MainMenuScreen;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.JsonConfigLoader;

import java.io.File;
import java.util.function.Consumer;

public class Main extends Game {
	public static boolean DEBUG;
	public static boolean DEBUG_UI;

	public static final String NAME = "evo_tree";
	public static String WORK_DIR;

	private static Main instance;
	private static final Logger log = LogManager.create(Main.class);

	private final AssetManager assetManager;
	private ConfigManager configManager;

	private SpriteBatch batch;
	private MainMenuScreen mainMenuScreen;

	public static Main createInstance() {
		instance = new Main();
		return instance;
	}

	public static Main getInstance() {
		return instance;
	}

	private Main() {
		this.assetManager = new AssetManager();
//		setJsonConfigLoader(class, function);
    }
	
	@Override
	public void create() {
		configManager = AbstractFactory.getInstance().configManager();
		configManager.init();

	    assetManager.load("image/texture_pack.atlas", TextureAtlas.class);
		loadUiAssets();

		assetManager.finishLoading();
		this.batch = new SpriteBatch();
		this.mainMenuScreen = new MainMenuScreen(batch);
		setScreen(mainMenuScreen);
		int width = configManager.getInteger("screenWidth");
		int height = configManager.getInteger("screenHeight");
		Gdx.graphics.setWindowedMode(width, height);

		if (WORK_DIR == null) {
			WORK_DIR = Gdx.files.getExternalStoragePath() + NAME;
		}
		FileHandle workDirFile = Gdx.files.absolute(WORK_DIR);
		if (!workDirFile.exists()) {
			workDirFile.mkdirs();
		}
		LogManager.init();
		log.info("INIT: Work dir = " + WORK_DIR);
		log.info("Main created");
	}

	public AbstractScreen getCurrentScreen() {
		return (AbstractScreen) screen;
	}

	public void goToMainMenu() {
	    setScreen(mainMenuScreen);
    }

    public AssetManager assets() {
		return assetManager;
	}

	/**
	 * Adds custom json loader to asset manager
	 * @param type To this class json data will be mapped
	 * @param onLoadHandler This function will be invoked after loading has finished. May be null.
	 * @param <T> Class of config
	 */
	private <T> void setJsonConfigLoader(Class<T> type, Consumer<T> onLoadHandler) {
		assetManager.setLoader(type, new JsonConfigLoader<>(assetManager.getFileHandleResolver(), type, onLoadHandler));
	}

	private void loadUiAssets() {
		assetManager.load("ui/uiskin.atlas", TextureAtlas.class);
		assetManager.load("ui/uiskin.json", Skin.class);
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
