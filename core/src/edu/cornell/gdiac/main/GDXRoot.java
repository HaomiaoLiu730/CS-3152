package edu.cornell.gdiac.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.main.controller.gaming.GameplayController;
import edu.cornell.gdiac.main.controller.gaming.MenuController;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.controller.opening.OnboardingController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class GDXRoot extends Game implements ScreenListener {

	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;

	private static final int NUMBER_OF_LEVELS = 1;
	private int current = 0;

	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	Loading loading;
	/** List of all WorldControllers */
	private WorldController[] controllers;
	/** menu controller*/
	private MenuController menuController;

	/**
	 * Creates a new game application root
	 */
	public GDXRoot() {}

	/**
	 * Called when the Application is first created.
	 *
	 * This method should always initialize the drawing context and begin asset loading.
	 */
	@Override
	public void create () {
		// Create the drawing context
		canvas  = new GameCanvas();
		loading = new OnboardingController(canvas, "gameSpecs.json");

		directory = new AssetDirectory("assets.json");
		directory.loadAssets();
		directory.finishLoading();
		controllers = new WorldController[NUMBER_OF_LEVELS];
		controllers[0] = new GameplayController();
		current = 0;
		menuController = new MenuController(canvas);
		loading.setScreenListener(this);
		setScreen(loading);
	}

	@Override
	public void dispose () {
		setScreen(null);
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	@Override
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void updateScreen(Screen screen, int exitCode) {
		if (screen instanceof OnboardingController) {
			for(int ii = 0; ii < controllers.length; ii++) {
				controllers[ii].loadContent(directory);
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);
			}
			loading.dispose();
			loading = null;
			menuController.setScreenListener(this);
			setScreen(menuController);
		} else if(screen instanceof MenuController){
			current = exitCode;
			menuController.dispose();
			menuController = null;
			controllers[current].reset();
			controllers[current].setScreenListener(this);
			setScreen(controllers[current]);
		}
	}
}
