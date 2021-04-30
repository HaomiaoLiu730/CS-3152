package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;
import java.util.HashMap;

public class  MenuController extends ClickListener implements Screen, InputProcessor, ControllerListener, Loading {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    private static final float MOUSE_TOL = 10f;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** is ready for game mode*/
    private boolean isReady = false;
    private static Color grey = new Color(1,1,1,0.5f);
    private Continent currentContinent;
    private boolean zoomIn;
    private int nextLevel = -1;
    private boolean drawPoints;
    private Camera camera;
    private boolean prevTouched;
    public enum Continent{
        NorthAmerica,
        SouthAmerica,
        Asia,
        Europe,
        Africa,
        Antarctica,
        Oceania
    }

    private static HashMap<Continent, ArrayList> finished = new HashMap<>();
    private static HashMap<Continent, Integer> numOfLevels = new HashMap<>();

    private static JsonValue value;

    private float[] AFRICA_LEVELS = new float[]{
            427f, 550f, 550, 540f, 680, 470f, 720, 350f, 700, 190
    };
    private float[] OCEANIA_LEVELS = new float[]{
            367, 324, 463, 300, 640, 335, 470, 400, 622, 422,
    };
    private float[] ASIA_LEVELS = new float[]{
            511, 530, 635, 500
            // 950, 520
    };
    private float[] NORTH_AMERICA_LEVELS = new float[]{
            1100f, 600f, 1000f, 610f
            // 300f, 470f
    };
    private float[] SOUTH_AMERICA_LEVELS = new float[]{
            500, 550
            // 530, 480, 640, 430
    };

    private AssetDirectory internal;

    private Texture background;
    private Texture northAmerica;
    private Texture southAmerica;
    private Texture africa;
    private Texture asia;
    private Texture europe;
    private Texture oceania;
    private Texture antarctica;
    private Texture antarcticaLine;
    private Texture northAmericaLine;
    private Texture southAmericaLine;
    private Texture asiaLine;
    private Texture africaLine;
    private Texture europeLine;
    private Texture oceaniaLine;

    private Sound menuSellect;
    private Sound menuScroll;


    private static ArrayList<Continent> unlockedContinents = new ArrayList<>();

    private BitmapFont gameFont;

    /**
     * Creates a new game with a playing field of the given size.
     * <p>
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     *
     */
    public MenuController(GameCanvas canvas) {
        internal = new AssetDirectory("menu/menuAsset.json");
        internal.loadAssets();
        internal.finishLoading();

        FileHandle file = Gdx.files.local("menu/levelProgress.json");
        JsonReader jsonReader = new JsonReader();
        value = jsonReader.parse(file);

        background = internal.getEntry("background", Texture.class);
        northAmerica = internal.getEntry("NorthAmerica", Texture.class);
        southAmerica = internal.getEntry("SouthAmerica", Texture.class);
        asia = internal.getEntry("Asia", Texture.class);
        europe = internal.getEntry("Europe", Texture.class);
        antarctica = internal.getEntry("Antarctica", Texture.class);
        africa = internal.getEntry("Africa", Texture.class);
        oceania = internal.getEntry("Oceania", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);
        antarcticaLine = internal.getEntry("AntarcticaLine", Texture.class);
        northAmericaLine = internal.getEntry("NorthAmericaLine", Texture.class);
        southAmericaLine = internal.getEntry("SouthAmericaLine", Texture.class);
        asiaLine = internal.getEntry("AsiaLine", Texture.class);
        oceaniaLine = internal.getEntry("OceaniaLine", Texture.class);
        europeLine = internal.getEntry("EuropeLine", Texture.class);
        africaLine = internal.getEntry("AfricaLine", Texture.class);
        menuSellect = internal.getEntry("menuSellect", SoundBuffer.class);
        menuScroll = internal.getEntry("menuScroll",SoundBuffer.class);

        active  = true;
        zoomIn = false;
        currentContinent = Continent.Africa;
        camera = canvas.getCamera();
        this.canvas = canvas;

        refreshMenu();
    }


    public static ArrayList<Integer> arrToArrList(int[] arr){
        ArrayList<Integer> ret = new ArrayList<Integer>(arr.length);
        for (int i : arr)
            ret.add(i);
        return ret;
    }

    public static void refreshMenu(){
        finished.put(Continent.NorthAmerica,arrToArrList(value.get("finished").get("NorthAmerica").asIntArray()));
        finished.put(Continent.SouthAmerica,arrToArrList(value.get("finished").get("SouthAmerica").asIntArray()));
        finished.put(Continent.Asia,arrToArrList(value.get("finished").get("Asia").asIntArray()));
        finished.put(Continent.Europe,arrToArrList(value.get("finished").get("Europe").asIntArray()));
        finished.put(Continent.Antarctica,arrToArrList(value.get("finished").get("Antarctica").asIntArray()));
        finished.put(Continent.Africa,arrToArrList(value.get("finished").get("Africa").asIntArray()));
        finished.put(Continent.Oceania,arrToArrList(value.get("finished").get("Oceania").asIntArray()));
        numOfLevels.put(Continent.NorthAmerica, value.get("numOfLevels").getInt("NorthAmerica"));
        numOfLevels.put(Continent.SouthAmerica, value.get("numOfLevels").getInt("SouthAmerica"));
        numOfLevels.put(Continent.Asia, value.get("numOfLevels").getInt("Asia"));
        numOfLevels.put(Continent.Europe, value.get("numOfLevels").getInt("Europe"));
        numOfLevels.put(Continent.Antarctica, value.get("numOfLevels").getInt("Antarctica"));
        numOfLevels.put(Continent.Africa, value.get("numOfLevels").getInt("Africa"));
        numOfLevels.put(Continent.Oceania, value.get("numOfLevels").getInt("Oceania"));
        unlockedContinents.add(Continent.Africa);
        for(Continent continent: finished.keySet()){
            if(finished.get(continent).size()!= 0 && finished.get(continent).get(finished.get(continent).size()-1) == numOfLevels.get(continent)){
                unlockedContinents.add(continent);
                switch (continent){
                    case Africa:
                        unlockContinents(Continent.Oceania);
                        break;
                }
            }
            if(finished.get(continent).size()!=0){
                unlockedContinents.add(continent);
            }
        }
    }

    public static void unlockContinents(Continent continent){
        unlockedContinents.add(continent);
    }

    private void zoomInto(float viewportWidth, float viewportHeight, float cameraPosX, float cameraPosY){
        float scale = Math.abs(cameraPosY - 360);
        float deltaWidth = (viewportWidth-1280)/scale;
        float deltaHeight = (viewportHeight-720)/scale;
        float deltaPosX = (cameraPosX - 640f)/scale;
        camera.viewportWidth = camera.viewportWidth >= viewportWidth ? camera.viewportWidth + deltaWidth : camera.viewportWidth;
        camera.viewportHeight = camera.viewportHeight >= viewportHeight ? camera.viewportHeight + deltaHeight : camera.viewportHeight;

        if(cameraPosX - 640 > 0){
            camera.position.x = camera.position.x <= cameraPosX ? camera.position.x + deltaPosX :camera.position.x;
        }else{
            camera.position.x = camera.position.x >= cameraPosX ? camera.position.x + deltaPosX :camera.position.x;
        }
        if(cameraPosY - 360 > 0){
            camera.position.y = camera.position.y <= cameraPosY ? camera.position.y + 1 :camera.position.y;
        }else{
            camera.position.y = camera.position.y >= cameraPosY ? camera.position.y - 1 :camera.position.y;
        }
        camera.update();
        if(Math.abs(camera.position.y - cameraPosY) <= 1){
            drawPoints = true;
            zoomIn = false;
        }
    }

    public void update(float delta) {
        zoomInEffect();
        selectContinent();
        InputController.getInstance().readInput();
        if(prevTouched && !Gdx.input.isTouched() && nextLevel != -1){
            isReady = true;
        }
        prevTouched = Gdx.input.isTouched();
        if(InputController.getInstance().touchUp() && unlockedContinents.contains(currentContinent)){
            zoomIn = true;
        }
        updateNextLevel();
    }

    public void selectContinent(){
        if(!zoomIn && !drawPoints){
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();
            Continent previousContinent = currentContinent;
            if(x>760 && x < 1280 && y > 70 && y < 370){
                currentContinent = Continent.NorthAmerica;
            }else if(x>1000 && x < 1280 && y > 370 && y < 620){
                currentContinent = Continent.SouthAmerica;
            }else if(x>340 && x < 740 && y > 120 && y < 376){
                currentContinent = Continent.Asia;
            }else if(x>60 && x < 340 && y > 150 && y <280){
                currentContinent = Continent.Europe;
            }else if(x>30 && x < 250 && y > 300 && y < 530){
                currentContinent = Continent.Africa;
            }else if(x>480 && x < 630 && y > 430 && y < 540){
                currentContinent = Continent.Oceania;
            }else if(x>0 && x < 690 && y > 650 && y < 720){
                currentContinent = Continent.Antarctica;
            }

            if(currentContinent != previousContinent)
                menuScroll.play();
        }
    }

    public void zoomInEffect(){
        if(zoomIn && unlockedContinents.contains(currentContinent)){
            switch (currentContinent){
                case NorthAmerica:
                    zoomInto(560f, 350f, 1000f, 480f);
                    break;
                case SouthAmerica:
                    zoomInto(560f, 300f, 1180f, 220f);
                    break;
                case Asia:
                    zoomInto(720f, 390f, 460f, 440f);
                    break;
                case Europe:
                    zoomInto(560f, 300f, 230f, 530f);
                    break;
                case Africa:
                    zoomInto(580f, 320f, 160f, 300f);
                    break;
                case Oceania:
                    zoomInto(420f, 230f,600f, 220f);
                    break;
                case Antarctica:
                    zoomInto(700f, 300f, 350f, 20f);
                    break;
                default:
                    break;
            }
        }
    }

    public void updateNextLevel(){
        if(drawPoints){
            int previousLevel = nextLevel;
            switch (currentContinent){
                case Africa:
                    updateNextLevelHelper(AFRICA_LEVELS);
                    break;
                case Oceania:
                    updateNextLevelHelper(OCEANIA_LEVELS);
                    break;
                case Asia:
                    updateNextLevelHelper(ASIA_LEVELS);
                    break;
                case NorthAmerica:
                    updateNextLevelHelper(NORTH_AMERICA_LEVELS);
                    break;
                case SouthAmerica:
                    updateNextLevelHelper(SOUTH_AMERICA_LEVELS);
                    break;
                default:
                    break;
            }
        }
    }

    public void updateNextLevelHelper(float[] arr){
        for(int i = 0; i< arr.length; i+=2){
            if (Math.abs(Gdx.input.getX() - arr[i]) <= MOUSE_TOL && Math.abs(720 - Gdx.input.getY() - arr[i+1]) <= MOUSE_TOL){
                nextLevel = i/2;
            }
        }
    }

    public void draw() {
        canvas.begin();
        canvas.drawOverlay(background,true);
        canvas.drawOverlay(northAmerica, unlockedContinents.contains(Continent.NorthAmerica)? Color.WHITE : grey, 0, 0);
        canvas.drawOverlay(southAmerica, unlockedContinents.contains(Continent.SouthAmerica)? Color.WHITE : grey,0,0);
        canvas.drawOverlay(oceania, unlockedContinents.contains(Continent.Oceania)? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(asia, unlockedContinents.contains(Continent.Asia)? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(europe, unlockedContinents.contains(Continent.Europe)? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(africa, unlockedContinents.contains(Continent.Africa)?Color.WHITE : grey,0, 0);
        canvas.drawOverlay(antarctica, unlockedContinents.contains(Continent.Antarctica)? Color.WHITE : grey,0, 0);
        if(drawPoints){
            int finishedLevelNum = finished.get(currentContinent).size();
            switch (currentContinent){
                case Africa:
                    drawPointsHelper(finishedLevelNum, AFRICA_LEVELS);
                    break;
                case Oceania:
                    drawPointsHelper(finishedLevelNum, OCEANIA_LEVELS);
                    break;
                case Asia:
                    drawPointsHelper(finishedLevelNum, ASIA_LEVELS);
                    break;
                case NorthAmerica:
                    drawPointsHelper(finishedLevelNum, NORTH_AMERICA_LEVELS);
                    break;
                case SouthAmerica:
                    drawPointsHelper(finishedLevelNum, SOUTH_AMERICA_LEVELS);
                    break;
                default:
                    break;
            }
        }
        drawCurrentContinent();
        canvas.end();
    }

    public void drawPointsHelper(int finishedLevelNum, float[] arr){
        for(int i = 0; i < finishedLevelNum*2; i+=2){
            canvas.drawCircle(Color.BLACK, arr[i], arr[i+1], nextLevel == i/2 ? 10f: 5f);
        }
        if(finishedLevelNum != numOfLevels.get(currentContinent)){
            canvas.drawCircle(Color.LIGHT_GRAY, arr[finishedLevelNum*2], arr[finishedLevelNum*2+1], nextLevel == finishedLevelNum ? 10f: 5f);
        }
    }

    public void drawCurrentContinent(){
        if(!zoomIn && !drawPoints){
            switch (currentContinent){
                case Antarctica:
                    canvas.drawOverlay(antarcticaLine,true);
                    break;
                case Africa:
                    canvas.drawOverlay(africaLine,true);
                    break;
                case Asia:
                    canvas.drawOverlay(asiaLine,true);
                    break;
                case Oceania:
                    canvas.drawOverlay(oceaniaLine,true);
                    break;
                case NorthAmerica:
                    canvas.drawOverlay(northAmericaLine,true);
                    break;
                case SouthAmerica:
                    canvas.drawOverlay(southAmericaLine,true);
                    break;
                case Europe:
                    canvas.drawOverlay(europeLine,true);
                    break;
            }
        }
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are are ready, notify our listener
            int hundred = 0;
            if(currentContinent == Continent.Africa) hundred = 2;
            if(currentContinent == Continent.Oceania) hundred = 3;
            if(currentContinent == Continent.Asia) hundred = 4;
            if(currentContinent == Continent.NorthAmerica) hundred = 5;
            if(currentContinent == Continent.SouthAmerica) hundred = 6;
            if (isReady() && listener != null) {
                listener.updateScreen(this, hundred*10+nextLevel);
                menuSellect.play();
            }
        }
    }

    public void reset(){
        zoomIn = false;
        drawPoints = false;
        canvas.getCamera().position.x = 640;
        canvas.getCamera().position.y = 360;
        canvas.getCamera().update();
        FileHandle file = Gdx.files.local("menu/levelProgress.json");
        JsonReader jsonReader = new JsonReader();
        value = jsonReader.parse(file);
        refreshMenu();
        isReady = false;
        nextLevel = -1;
        for(Continent continent: finished.keySet()){
            if(finished.get(continent).size()!= 0 && finished.get(continent).get(finished.get(continent).size()-1) == numOfLevels.get(continent)){
                if(continent != Continent.Asia){
                    unlockedContinents.add(continent);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public AssetDirectory getAssets() {
        return null;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
