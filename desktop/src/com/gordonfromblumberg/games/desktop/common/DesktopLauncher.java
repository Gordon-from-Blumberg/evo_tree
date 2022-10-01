package com.gordonfromblumberg.games.desktop.common;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.desktop.common.factory.DesktopFactory;

public class DesktopLauncher {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.foregroundFPS = 0;
//		config.vSyncEnabled = false;
		config.title = Main.NAME;

		DesktopFactory.init();
		ConfigManager cfgMgr = AbstractFactory.getInstance().configManager();

		for (String arg : args) {
			if ("-debug".equals(arg)) {
				Main.DEBUG = true;
				continue;
			}
			if ("-debugui".equals(arg)) {
				Main.DEBUG_UI = true;
				continue;
			}
			if ("-lightingTest".equals(arg)) {
				cfgMgr.setBoolean("lightingTest", true);
				continue;
			}
		}

		new LwjglApplication(Main.createInstance(), config);
	}
}
