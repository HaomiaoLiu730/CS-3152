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
	public static final int GAMEPLAY_CONTINUE = 130;
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
	private int totalLevels;

	private GameplayController levelEditorGameplayController;
	/** menu controller*/
	private MenuController menuController;
	private LevelEditorController levelEditor;
	private static JsonValue value;
	private static HashMap<MenuController.Continent, Integer> numOfLevels = new HashMap<>();
	private MenuController.Continent currentContinent = MenuController.Continent.Europe;
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
		totalLevels = 0;
		for(MenuController.Continent continent: MenuController.Continent.values()){
			totalLevels += numOfLevels.get(continent);
		}
		int prevLevels = 0;
		controllers = new WorldController[totalLevels];
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.Europe); i++){
			controllers[i+prevLevels] = new GameplayController("europe/europeMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
		prevLevels += numOfLevels.get(MenuController.Continent.Europe);
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
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.Asia); i++){
			controllers[i+prevLevels] = new GameplayController("asia/asiaMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
		prevLevels += numOfLevels.get(MenuController.Continent.Asia);
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.NorthAmerica); i++){
			controllers[i+prevLevels] = new GameplayController("NorthAmerica/northAmericaMain.json",i);
			controllers[i+prevLevels].setScreenListener(this);
		}
		prevLevels += numOfLevels.get(MenuController.Continent.NorthAmerica);
		for(int i = 0; i < numOfLevels.get(MenuController.Continent.SouthAmerica); i++){
			controllers[i+prevLevels] = new GameplayController("southAmerica/southAmericaMain.json",i);
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

	public void nextLevel(){
		controllers[current].loadContent(directory);
		controllers[current].setScreenListener(this);
		controllers[current].setCanvas(canvas);
		controllers[current].reset();
		setScreen(controllers[current]);
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
			else if (exitCode==2){
				if( value.getInt("next")<current) {
					value.get("next").remove();
					value.addChild("next",new JsonValue(current));

				}
				current=value.getInt("next");
				controllers[current].loadContent(directory);
				controllers[current].setScreenListener(this);
				controllers[current].setCanvas(canvas);
				controllers[current].reset();
				setScreen(controllers[current]);

				if(current == numOfLevels.get(currentContinent)){
					// switch to the second level
					switch (currentContinent){
						case Europe:
							currentContinent = MenuController.Continent.Africa;
							MenuController.unlockContinents(MenuController.Continent.Africa);
							break;
						case Africa:
							currentContinent = MenuController.Continent.Oceania;
							MenuController.unlockContinents(MenuController.Continent.Oceania);
							break;
						case Oceania:
							currentContinent = MenuController.Continent.Asia;
							MenuController.unlockContinents(MenuController.Continent.Asia);
							break;
						case Asia:
							currentContinent = MenuController.Continent.NorthAmerica;
							MenuController.unlockContinents(MenuController.Continent.NorthAmerica);
						case NorthAmerica:
							currentContinent = MenuController.Continent.SouthAmerica;
							MenuController.unlockContinents(MenuController.Continent.SouthAmerica);
						default:
							break;
					}
				}

			}
		} else if(screen instanceof MenuController){
			int hundred = exitCode/10;
			if(hundred == 1){
				currentContinent = MenuController.Continent.Europe;
				current = exitCode%10;
			}else if(hundred == 2){
				current = numOfLevels.get(MenuController.Continent.Europe)+(exitCode%10);
				currentContinent = MenuController.Continent.Africa;
			}else if(hundred == 3){
				current = numOfLevels.get(MenuController.Continent.Europe)
						+ numOfLevels.get(MenuController.Continent.Africa)
						+ (exitCode%10);
				currentContinent = MenuController.Continent.Oceania;
			}else if(hundred == 4){
				current = numOfLevels.get(MenuController.Continent.Europe)
						+ numOfLevels.get(MenuController.Continent.Africa)
						+ numOfLevels.get(MenuController.Continent.Oceania)
						+(exitCode%10);
				currentContinent = MenuController.Continent.Asia;
			}else if(hundred == 5){
				current = numOfLevels.get(MenuController.Continent.Europe)
						+ numOfLevels.get(MenuController.Continent.Africa)
						+ numOfLevels.get(MenuController.Continent.Oceania)
						+ numOfLevels.get(MenuController.Continent.Asia)
						+(exitCode%10);
				currentContinent = MenuController.Continent.NorthAmerica;
			}else if(hundred == 6){
				current = numOfLevels.get(MenuController.Continent.Europe)
						+ numOfLevels.get(MenuController.Continent.Africa)
						+ numOfLevels.get(MenuController.Continent.Oceania)
						+ numOfLevels.get(MenuController.Continent.Asia)
						+ numOfLevels.get(MenuController.Continent.NorthAmerica)
						+(exitCode%10);
				currentContinent = MenuController.Continent.SouthAmerica;
			}
			if( value.getInt("next")<current) {
				value.get("next").remove();
				value.addChild("next",new JsonValue(current));

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
		} else if(screen instanceof GameplayController){
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
				if(exitCode!=GAMEPLAY_CONTINUE){
					controllers[current].dispose();
				}
				// write progress to json
				current++;
				int[] finished = value.get("finished").get(currentContinent.name()).asIntArray();
				int addedVal = 0;
				if(currentContinent == MenuController.Continent.Europe) {
					addedVal = current;
				}else if(currentContinent == MenuController.Continent.Africa){
					addedVal = current - numOfLevels.get(MenuController.Continent.Europe);
				}else if(currentContinent == MenuController.Continent.Oceania){
					addedVal = current - numOfLevels.get(MenuController.Continent.Europe)
							- numOfLevels.get(MenuController.Continent.Africa);
				}else if(currentContinent == MenuController.Continent.Asia){
					addedVal = current - numOfLevels.get(MenuController.Continent.Europe)
							- numOfLevels.get(MenuController.Continent.Africa)
							- numOfLevels.get(MenuController.Continent.Oceania);
				}else if(currentContinent == MenuController.Continent.NorthAmerica){
					addedVal = current - numOfLevels.get(MenuController.Continent.Europe)
							- numOfLevels.get(MenuController.Continent.Africa)
							- numOfLevels.get(MenuController.Continent.Oceania)
							- numOfLevels.get(MenuController.Continent.Asia);
				}else if(currentContinent == MenuController.Continent.SouthAmerica){
					addedVal = current - numOfLevels.get(MenuController.Continent.Europe)
							- numOfLevels.get(MenuController.Continent.Africa)
							- numOfLevels.get(MenuController.Continent.Oceania)
							- numOfLevels.get(MenuController.Continent.Asia)
							- numOfLevels.get(MenuController.Continent.NorthAmerica);
				}
				if(exitCode == GAMEPLAY_CONTINUE){
					if(finished.length > addedVal){
						nextLevel();
						return;
					}
					if(addedVal == numOfLevels.get(currentContinent)){
						switch (currentContinent){
							case Europe:
								currentContinent = MenuController.Continent.Africa;
								MenuController.unlockContinents(MenuController.Continent.Africa);
							case Africa:
								if(value.get("finished").get(MenuController.Continent.Oceania.name()).asIntArray().length != 0) {
									currentContinent = MenuController.Continent.Oceania;
									nextLevel();
								}
								break;
							case Oceania:
								if(value.get("finished").get(MenuController.Continent.Asia.name()).asIntArray().length != 0) {
									currentContinent = MenuController.Continent.Asia;
									nextLevel();
								}
								break;
							case Asia:
								if(value.get("finished").get(MenuController.Continent.NorthAmerica.name()).asIntArray().length != 0) {
									currentContinent = MenuController.Continent.NorthAmerica;
									nextLevel();
								}
								break;
							case NorthAmerica:
								if(value.get("finished").get(MenuController.Continent.SouthAmerica.name()).asIntArray().length != 0) {
									currentContinent = MenuController.Continent.SouthAmerica;
									nextLevel();
								}
								break;
							case SouthAmerica:
								current --;
								nextLevel();
								if(value.get("finished").get(MenuController.Continent.Antarctica.name()).asIntArray().length != 0) {
									currentContinent = MenuController.Continent.Antarctica;
									nextLevel();
								}
								break;
							default:
								break;
						}
						return;
					}
					current --;
					nextLevel();
					return;
				}
				if(finished.length == 0 || finished[finished.length-1] < addedVal){
					value.get("finished").get(currentContinent.name()).addChild(new JsonValue(addedVal));
					FileHandle file = Gdx.files.local("menu/levelProgress.json");
					file.writeString(value.prettyPrint(JsonWriter.OutputType.json,0), false);
				}

				if(current == numOfLevels.get(currentContinent)){
					// switch to the second level
					switch (currentContinent){
						case Europe:
							currentContinent = MenuController.Continent.Africa;
							MenuController.unlockContinents(MenuController.Continent.Africa);
						case Africa:
							currentContinent = MenuController.Continent.Oceania;
							MenuController.unlockContinents(MenuController.Continent.Oceania);
							break;
						case Oceania:
							currentContinent = MenuController.Continent.Asia;
							MenuController.unlockContinents(MenuController.Continent.Asia);
							break;
						case Asia:
							currentContinent = MenuController.Continent.NorthAmerica;
							MenuController.unlockContinents(MenuController.Continent.NorthAmerica);
						case NorthAmerica:
							currentContinent = MenuController.Continent.SouthAmerica;
							MenuController.unlockContinents(MenuController.Continent.SouthAmerica);
						default:
							break;
					}
				}
				if(current == totalLevels-1){
					menuController.reset();
					menuController.setScreenListener(this);
					setScreen(menuController);
					return;
				}else{
					if( value.getInt("next")<current) {
						value.get("next").remove();
						value.addChild("next",new JsonValue(current));

					}
					FileHandle file = Gdx.files.local("menu/levelProgress.json");
					file.writeString(value.prettyPrint(JsonWriter.OutputType.json,0), false);
					// controllers[current].loadContent(directory);
					// controllers[current].setScreenListener(this);
					// controllers[current].setCanvas(canvas);
					// controllers[current].reset();
					// setScreen(controllers[current]);
					nextLevel();
				}
			}
		}
	}
}
