package edu.cornell.gdiac.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;
import edu.cornell.gdiac.main.GDXRoot;

public class DesktopLauncher {
	private static final int SCREEN_WIDTH = 1280;
	private static final int SCREEN_HEIGHT = 720;
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		config.width = SCREEN_WIDTH;
		config.height = SCREEN_HEIGHT;
		new GDXApp(new GDXRoot(), config);
	}
}
