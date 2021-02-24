package edu.cornell.gdiac.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.gaming.GameMode;
import edu.cornell.gdiac.main.controller.gaming.LevelLoadingMode;
import edu.cornell.gdiac.main.controller.opening.GameSpecMode;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.controller.opening.OnboardingMode;
import edu.cornell.gdiac.main.view.GameCanvas;

public class GDXRoot extends ApplicationAdapter {

	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;

	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	Loading loading;
	/** Polymorphic reference to the active player mode */
	ModeController controller;

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
	}

	@Override
	public void render () {
		if (loading != null && loading.isReady()) {
			if(loading instanceof OnboardingMode){
				loading = new GameSpecMode(canvas.getWidth(),canvas.getHeight());
				controller = loading;
			}else if(loading instanceof GameSpecMode && loading.isReady()){
				directory = loading.getAssets();
				loading.dispose();
				loading = null;
				controller = new GameMode(canvas.getWidth(),canvas.getHeight(),directory);
			}
		}
		// Update the game state
		controller.update();

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		canvas.begin();
		controller.draw(canvas);
		controller.update();
		canvas.end();
	}
	
	@Override
	public void dispose () {
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
}
