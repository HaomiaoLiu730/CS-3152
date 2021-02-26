package edu.cornell.gdiac.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.main.controller.gaming.GameMode;
import edu.cornell.gdiac.main.controller.gaming.LevelLoadingMode;
import edu.cornell.gdiac.main.controller.opening.GameSpecMode;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.controller.opening.OnboardingMode;
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
	/** Polymorphic reference to the active player mode */
	ModeController controller;
	/** List of all WorldControllers */
	private WorldController[] controllers;

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
		loading = new OnboardingMode("gameSpecs.json");
		controller = loading;
//		controller = new LevelLoadingMode(false);
		directory = new AssetDirectory("assets.json");
		controllers = new WorldController[NUMBER_OF_LEVELS];
		controllers[0] = new GameMode();
		controllers[0].setScreenListener(this);
		controllers[current].loadContent(directory);
	}

	@Override
	public void render () {
//		if (loading != null && loading.isReady()) {
//			if(loading instanceof OnboardingMode){
//				loading = new GameSpecMode(canvas.getWidth(),canvas.getHeight());
//				controller = loading;
//			}else if(loading instanceof GameSpecMode){
//				loading.dispose();
//				loading = null;
//				controller = new GameMode(canvas.getWidth(),canvas.getHeight());
////				controllers[current].setScreenListener(this);
////				controllers[current].setCanvas(canvas);
////				controllers[current].reset();
////				setScreen(controllers[current]);
//			}
//		}
//		// Update the game state
//		controller.update();
//
//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		canvas.begin();
//		controller.draw(canvas);
//		controller.update();
//		canvas.end();
		if(loading != null){
			controller.update();
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			canvas.begin();
			controller.draw(canvas);
			controller.update();
			canvas.end();
		}
		if (loading != null && loading.isReady() && loading instanceof OnboardingMode) {
			loading = new GameSpecMode(canvas.getWidth(), canvas.getHeight());
			controller = loading;
			// Update the game state
			controller.update();

			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			canvas.begin();
			controller.draw(canvas);
			controller.update();
			canvas.end();
		}
		else if(loading != null && loading.isReady() && loading instanceof GameSpecMode){
			loading.dispose();
			loading = null;
			controller = new GameMode(canvas.getWidth(),canvas.getHeight());
			controllers[current].setScreenListener(this);
			controllers[current].setCanvas(canvas);
			controllers[current].reset();
			setScreen(controllers[current]);
		}
	}
	
	@Override
	public void dispose () {
		setScreen(null);
		controller.dispose();
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
	 * before a call to create()
	 *
	 * @param width The window width
	 * @param height The window height
	 */
	@Override
	public void resize(int width, int height) {
		if (controller != null) {
			controller.resize(width,height);
		}
		// Canvas knows the size, but not that it changed
		canvas.resize();
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
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			for(int ii = 0; ii < controllers.length; ii++) {
				controllers[ii].loadContent(directory);
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);
			}
			controllers[current].reset();
			setScreen(controllers[current]);

			loading.dispose();
			loading = null;
		} else if (exitCode == WorldController.EXIT_NEXT) {
			current = (current+1) % controllers.length;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == WorldController.EXIT_PREV) {
			current = (current+controllers.length-1) % controllers.length;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == WorldController.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}
}
