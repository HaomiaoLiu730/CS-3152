package edu.cornell.gdiac.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.LevelEditorController;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.main.controller.gaming.GameplayController;
import edu.cornell.gdiac.main.controller.gaming.MenuController;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.controller.opening.OnboardingController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.HashMap;

public class GDXRoot extends Game implements ScreenListener {

	public static final int EDITOR_GAMEPLAY = 128;
	public static final int GAMEPLAY_EDITOR = 129;
	public static final int GAMEPLAY_MENU = 150;

	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;

	private int current = 0;

	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	Loading loading;
	/** List of all WorldControllers */
	private WorldController[] controllers;

	private GameplayController levelEditorGameplayController;
	/** menu controller*/
	private MenuController menuController;
	private LevelEditorController levelEditor;
	private static JsonValue value;
	private static HashMap<MenuController.Continent, Integer> numOfLevels = new HashMap<>();
	private MenuController.Continent currentContinent = MenuController.Continent.Africa;
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
		FileHandle file = Gdx.files.local("menu/levelProgress.json");
		JsonReader jsonReader = new JsonReader();
		value = jsonReader.parse(file);

		numOfLevels.put(MenuController.Continent.NorthAmerica, value.get("numOfLevels").getInt("NorthAmerica"));
		numOfLevels.put(MenuController.Continent.SouthAmerica, value.get("numOfLevels").getInt("SouthAmerica"));
		numOfLevels.put(MenuController.Continent.Asia, value.get("numOfLevels").getInt("Asia"));
		numOfLevels.put(MenuController.Continent.Europe, value.get("numOfLevels").getInt("Europe"));
		numOfLevels.put(MenuController.Continent.Antarctica, value.get("numOfLevels").getInt("Antarctica"));
		numOfLevels.put(MenuController.Continent.Africa, value.get("numOfLevels").getInt("Africa"));
		numOfLevels.put(MenuController.Continent.Oceania, value.get("numOfLevels").getInt("Oceania"));

		// Create the drawing context
		canvas  = new GameCanvas();
		loading = new OnboardingController(canvas, "gameSpecs.json");

		directory = new AssetDirectory("assets.json");
		directory.loadAssets();
		directory.finishLoading();
		int totalLevels = 0;
		for(MenuController.Continent continent: MenuController.Continent.values()){
			totalLevels += numOfLevels.get(continent);
		}
		int prevLevels = 0;
		controllers = new WorldController[totalLevels];
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.Africa); i++){
			controllers[i+prevLevels] = new GameplayController("africa/africaMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
		prevLevels += numOfLevels.get(MenuController.Continent.Africa);
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.Oceania); i++){
			controllers[i+prevLevels] = new GameplayController("oceania/oceaniaMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
		prevLevels += numOfLevels.get(MenuController.Continent.Oceania);
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.NorthAmerica); i++){
			controllers[i+prevLevels] = new GameplayController("NorthAmerica/northAmericaMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
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
			if(exitCode == 0){
				loading.dispose();
				loading = null;
				menuController.setScreenListener(this);
				setScreen(menuController);
			}else if(exitCode == 1){
				levelEditor = new LevelEditorController(canvas);
				levelEditor.setScreenListener(this);
				setScreen(levelEditor);
			}
		} else if(screen instanceof MenuController){
			int hundred = exitCode/10;
			if(hundred == 1){
				currentContinent = MenuController.Continent.Europe;
				current = exitCode%10;
			}else if(hundred == 2){
				current = exitCode%10;
				currentContinent = MenuController.Continent.Africa;
			}else if(hundred == 3){
				current = numOfLevels.get(MenuController.Continent.Africa)+(exitCode%10);
				currentContinent = MenuController.Continent.Oceania;
			}
			controllers[current].loadContent(directory);
			controllers[current].setScreenListener(this);
			controllers[current].setCanvas(canvas);
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if(screen instanceof LevelEditorController){
			if(exitCode == EDITOR_GAMEPLAY){
				levelEditorGameplayController = new GameplayController(true, "levelEditor.json",0);
				levelEditorGameplayController.loadContent(directory);
				levelEditorGameplayController.setCanvas(canvas);
				levelEditorGameplayController.reset();
				levelEditorGameplayController.setScreenListener(this);
				setScreen(levelEditorGameplayController);
			}
		}else if(screen instanceof GameplayController){
			if (exitCode == GAMEPLAY_MENU) {
				controllers[current].dispose();
				menuController.reset();
				menuController.setScreenListener(this);
				setScreen(menuController);
			}else if(exitCode == GAMEPLAY_EDITOR){
				levelEditorGameplayController.reset();
				levelEditorGameplayController.setScreenListener(this);
				setScreen(levelEditor);
			}else{
				controllers[current].dispose();
				// write progress to json
				current++;
				int[] finished = value.get("finished").get(currentContinent.name()).asIntArray();
				int addedVal = 0;
				if(current == numOfLevels.get(currentContinent)){
					// switch to the second level
					switch (currentContinent){
						case Africa:
							currentContinent = MenuController.Continent.Oceania;
							addedVal = current;
							MenuController.unlockContinents(MenuController.Continent.Oceania);
							break;
						case Oceania:
							currentContinent = MenuController.Continent.Asia;
							addedVal = current - numOfLevels.get(MenuController.Continent.Africa);
							break;
						default:
							break;
					}
				}
				if(finished.length == 0 || finished[finished.length-1] < current){
					value.get("finished").get(currentContinent.name()).addChild(new JsonValue(addedVal));
					FileHandle file = Gdx.files.local("menu/levelProgress.json");
					file.writeString(value.prettyPrint(JsonWriter.OutputType.json,0), false);
				}
				controllers[current].loadContent(directory);
				controllers[current].setScreenListener(this);
				controllers[current].setCanvas(canvas);
				controllers[current].reset();
				setScreen(controllers[current]);
			}
		}
	}
}
