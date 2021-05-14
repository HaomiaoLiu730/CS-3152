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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private static final float MOUSE_TOL = 24f;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** is ready for game mode*/
    private boolean isReady = false;
    private static Color grey = new Color(1,1,1,0.5f);
    private Continent currentContinent;
    private boolean zoomIn;
    private boolean zoomOut;
    private int nextLevel = -1;
    private boolean drawPoints;
    private Camera camera;
    private boolean prevTouched;
    private float zoomInTime = 0;
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

    private float[] EUROPE_LEVELS = new float[]{
            852f, 427f,757,426, 770,347, 690,342, 580,300
            //720, 340. 333, 511, 474, 490, 600, 470
    };
    private float[] AFRICA_LEVELS = new float[]{
            450, 417, 564, 475, 682, 453
            //720, 350f, 700, 190
    };
    private float[] OCEANIA_LEVELS = new float[]{
            530, 384, 613, 428, 731, 393
            // 470, 400, 622, 422,
    };
    private float[] ASIA_LEVELS = new float[]{
            511, 500, 635, 470, 950, 490, 760, 260, 620, 270, 543, 270, 433, 270
    };
    private float[] NORTH_AMERICA_LEVELS = new float[]{
            1050f, 600f, 950f, 610f, 350f, 470f, 550f, 300f, 650f, 300f, 720f, 320f,600, 200f
    };
    private float[] SOUTH_AMERICA_LEVELS = new float[]{
            620, 550, 650, 480, 760, 430, 720, 360, 660, 320, 620, 280
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
    private Texture penguin1;
    private Texture penguin2;
    private Texture penguin3;
    private Texture penguin4;
    private Texture penguin5;
    private Texture penguin6;
    private Texture penguin7;
    private TextureRegion backArrowTexture;
    private Texture[] penguins = new Texture[7];

    private Sound menuSellect;
    private Sound menuScroll;
    private Sound menuBackground;

    private boolean isMenuBPlaying;


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
        menuBackground = internal.getEntry("menuBackground",SoundBuffer.class);
        menuScroll = internal.getEntry("menuScroll",SoundBuffer.class);
        penguin1 = internal.getEntry("penguin1", Texture.class);
        penguin2 = internal.getEntry("penguin2", Texture.class);
        penguin3 = internal.getEntry("penguin3", Texture.class);
        penguin4 = internal.getEntry("penguin4", Texture.class);
        penguin5 = internal.getEntry("penguin5", Texture.class);
        penguin6 = internal.getEntry("penguin6", Texture.class);
        penguin7 = internal.getEntry("penguin7", Texture.class);
        backArrowTexture = new TextureRegion(internal.getEntry("backArrow", Texture.class));
        penguins[0] = penguin1;
        penguins[1] = penguin2;
        penguins[2] = penguin3;
        penguins[3] = penguin4;
        penguins[4] = penguin5;
        penguins[5] = penguin6;
        penguins[6] = penguin7;

        active  = true;
        zoomIn = false;
        currentContinent = Continent.Europe;
        camera = canvas.getCamera();
        this.canvas = canvas;

        isMenuBPlaying = false;

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
        unlockedContinents.add(Continent.Europe);
        for(Continent continent: finished.keySet()){
            if(finished.get(continent).size()!= 0 && finished.get(continent).get(finished.get(continent).size()-1) == numOfLevels.get(continent)){
                unlockedContinents.add(continent);
            }
            if(finished.get(continent).size()!=0){
                unlockedContinents.add(continent);
            }
        }
    }

    public static void unlockContinents(Continent continent){
        unlockedContinents.add(continent);
    }

    public float quadraticFunction(float a, float b, float c, float t){
        return a*t*t+b*t+c;
    }

    private void zoomInto(float viewportWidth, float viewportHeight, float cameraPosX, float cameraPosY){
        zoomInTime+=0.1;
        float deltaPosY = quadraticFunction(0.06f,0.05f,0,zoomInTime);
        float scale = Math.abs(cameraPosY - 360);
        float deltaWidth = (viewportWidth-1280)/scale*deltaPosY;
        float deltaHeight = (viewportHeight-720)/scale*deltaPosY;
        float deltaPosX = (cameraPosX - 640f)/scale*deltaPosY;

        camera.viewportWidth = camera.viewportWidth + deltaWidth;
        camera.viewportHeight = camera.viewportHeight + deltaHeight;
        camera.position.x = camera.position.x + deltaPosX;
        if(cameraPosY - 360 > 0){
            camera.position.y = camera.position.y + deltaPosY;
        }else{
            camera.position.y = camera.position.y - deltaPosY;
        }
        camera.update();
        if(Math.abs(camera.position.y - cameraPosY) <= 5){
            drawPoints = true;
            zoomIn = false;
            zoomInTime = 0;
        }
    }


    private float[] getCoordinate(float x, float y, Continent c) {
        float width = 640f;
        float height = 360f;
        float[] result = new float[2];
        if (c == Continent.Europe) {
            result[0] = x*width/1280+312;
            result[1] = y*height/720+325;
        } else if (c == Continent.NorthAmerica) {
            result[0] = x*width/1280+310;
            result[1] = y*height/720+293;
        } else if (c == Continent.SouthAmerica) {
            result[0] = x*width/1280+297;
            result[1] = y*height/720+47;
        } else if (c == Continent.Asia) {
            result[0] = x*width/1280+312;
            result[1] = y*height/720+285;
        } else if (c == Continent.Africa) {
            result[0] = x*width/1280+310;
            result[1] = y*height/720+155;
        } else if (c == Continent.Oceania) {
            result[0] = x*width/1280 + 312;
            result[1] = y*height/720 + 30;
        } else {
            result[0] = x*width/1280-350+640;
            result[1] = y*height/720+400;
        }
        return result;
    }

    public void update(float delta) {
        zoomInEffect();
        zoomOutEffect();
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
        if(InputController.getInstance().touchUp() && Math.abs(Gdx.input.getX()-100)<40 && Math.abs(Gdx.input.getY()-60)<40){
            zoomOut = true;
            isReady = false;
        }
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
                case Europe:
                    zoomInto(640f, 360f, 205f, 510f);
                    break;
                case NorthAmerica:
                    zoomInto(640f, 360f, 1050f, 480f);
                    break;
                case SouthAmerica:
                    zoomInto(640f, 360f, 1140f, 220f);
                    break;
                case Asia:
                    zoomInto(640f, 360f, 455f, 470f);
                    break;
                case Africa:
                    zoomInto(640f, 360f, 70f, 330f);
                    break;
                case Oceania:
                    zoomInto(640f, 360f,560f, 210f);
                    break;
                case Antarctica:
                    zoomInto(640f, 360f, 350f, 20f);
                    break;
                default:
                    break;
            }
        }
    }

    public void zoomOutEffect(){
        if(zoomOut) {
            drawPoints = false;
            zoomIn = false;
            zoomInTime += 0.1;
            float cameraPosX = 640;
            float cameraPosY = 360;
            float deltaPosY = quadraticFunction(0.06f, 0.05f, 0, zoomInTime);
            float scale = Math.abs(cameraPosX - 360);
            float deltaWidth = (1280 - camera.viewportWidth) / scale * deltaPosY;
            float deltaHeight = (720 - camera.viewportHeight) / scale * deltaPosY;
            float deltaPosX = Math.abs(cameraPosX - camera.position.x)/scale*deltaPosY;
            camera.viewportWidth = camera.viewportWidth + deltaWidth;
            camera.viewportHeight = camera.viewportHeight + deltaHeight;

            if (camera.position.x - 640 > 0) {
                camera.position.x = camera.position.x > cameraPosX ? camera.position.x - deltaPosX : camera.position.x;
            } else {
                camera.position.x = camera.position.x < cameraPosX ? camera.position.x + deltaPosX : camera.position.x;
            }
            if (camera.position.y - 360 > 5) {
                camera.position.y = camera.position.y > cameraPosY ? camera.position.y - deltaPosY : camera.position.y;
            } else {
                camera.position.y = camera.position.y < cameraPosY ? camera.position.y + deltaPosY : camera.position.y;
            }
            camera.update();
            if (Math.abs(camera.position.x - cameraPosX) <= 2) {
                zoomInTime = 0;
                zoomOut = false;
                this.reset();
            }
        }
    }

    public void updateNextLevel(){
        if(drawPoints){
            int previousLevel = nextLevel;
            switch (currentContinent){
                case Europe:
                    updateNextLevelHelper(EUROPE_LEVELS);
                    break;
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

    private void drawWorldMap(float x, float y) {
        canvas.drawOverlay(background,x, y);
        canvas.drawOverlay(northAmerica, unlockedContinents.contains(Continent.NorthAmerica)? Color.WHITE : grey, x, y);
        canvas.drawOverlay(southAmerica, unlockedContinents.contains(Continent.SouthAmerica)? Color.WHITE : grey,x,y);
        canvas.drawOverlay(oceania, unlockedContinents.contains(Continent.Oceania)? Color.WHITE : grey,x, y);
        canvas.drawOverlay(asia, unlockedContinents.contains(Continent.Asia)? Color.WHITE : grey,x, y);
        canvas.drawOverlay(europe, unlockedContinents.contains(Continent.Europe)? Color.WHITE : grey,x, y);
        canvas.drawOverlay(africa, unlockedContinents.contains(Continent.Africa)?Color.WHITE : grey,x, y);
        canvas.drawOverlay(antarctica, unlockedContinents.contains(Continent.Antarctica)? Color.WHITE : grey,x, y);
    }

    public void draw() {
        canvas.begin();

        drawWorldMap(-1275,720);
        drawWorldMap(-1275,0);
        drawWorldMap(-1275,-720);
        drawWorldMap(0,720);
        drawWorldMap(0,-720);
        drawWorldMap(0,0);
        drawWorldMap(1275,720);
        drawWorldMap(1275,0);
        drawWorldMap(1275,-720);


        if(drawPoints){
            int finishedLevelNum = finished.get(currentContinent).size();
            canvas.drawFixed(backArrowTexture);
            switch (currentContinent){
                case Europe:
                    drawPointsHelper(finishedLevelNum, EUROPE_LEVELS, 1);
                    break;
                case Africa:
                    drawPointsHelper(finishedLevelNum, AFRICA_LEVELS, 2);
                    break;
                case Oceania:
                    drawPointsHelper(finishedLevelNum, OCEANIA_LEVELS, 3);
                    break;
                case Asia:
                    drawPointsHelper(finishedLevelNum, ASIA_LEVELS, 4);
                    break;
                case NorthAmerica:
                    drawPointsHelper(finishedLevelNum, NORTH_AMERICA_LEVELS, 5);
                    break;
                case SouthAmerica:
                    drawPointsHelper(finishedLevelNum, SOUTH_AMERICA_LEVELS, 6);
                    break;
                default:
                    break;
            }
        }
        drawCurrentContinent();
        if (!zoomIn && !drawPoints && !zoomOut) {
            canvas.draw(penguin5, Color.WHITE, 960f, 460f, penguin5.getWidth()/2, penguin5.getHeight()/2);
            canvas.drawText(gameFont, "5", 990f, 525f);
            canvas.draw(penguin6, Color.WHITE, 1115f, 260f, penguin6.getWidth()/2, penguin6.getHeight()/2);
            canvas.drawText(gameFont, "6", 1130f, 340f);
            canvas.draw(penguin4, Color.WHITE, 410f, 470f, penguin4.getWidth()/2, penguin4.getHeight()/2);
            canvas.drawText(gameFont, "4", 460f, 515f);
            canvas.draw(penguin1, Color.WHITE, 200f, 490f, penguin1.getWidth()/2, penguin1.getHeight()/2);
            canvas.drawText(gameFont, "1", 238f, 560f);
            canvas.draw(penguin2, Color.WHITE, 120f, 320f, penguin2.getWidth()/2, penguin2.getHeight()/2);
            canvas.drawText(gameFont, "2", 163f, 400f);
            canvas.draw(penguin3, Color.WHITE, 525f, 205f, penguin3.getWidth()/2, penguin3.getHeight()/2);
            canvas.drawText(gameFont, "3", 562f, 300f);
            canvas.draw(penguin7, Color.WHITE, 350f, 15f, penguin7.getWidth()/2, penguin7.getHeight()/2);
            canvas.drawText(gameFont, "7", 405f, 65f);
            gameFont.getData().setScale(2f);
            canvas.drawText(gameFont, "World Map", 580f, 680f);
            gameFont.getData().setScale(1f);
        }
        canvas.end();
    }

    public void drawPointsHelper(int finishedLevelNum, float[] arr, int continent){
        for(int i = 0; i < finishedLevelNum*2; i+=2){
            canvas.drawEllipse(Color.BLACK, arr[i], arr[i+1], nextLevel == i/2 ? 75f/2: 25f, nextLevel == i/2 ? 45f/2: 15);
            //canvas.drawText(gameFont, String.valueOf((i+2)/2), arr[i]*camera.viewportWidth/1280+Math.abs(640-camera.position.x)+camera.position.x-640f, arr[i+1]*camera.viewportHeight/720+Math.abs(360-camera.position.y)+camera.position.y-360f);
            gameFont.getData().setScale(0.75f);
            canvas.drawText(gameFont, continent+"-"+(i+2)/2, getCoordinate(arr[i]+13f, arr[i+1]-5f, currentContinent)[0]+camera.position.x-640f, getCoordinate(arr[i]+13f, arr[i+1]-5f, currentContinent)[1]);
            gameFont.getData().setScale(1f);
            if (i+3 < numOfLevels.get(currentContinent)*2){
                canvas.drawDottedLine(6, arr[i]+13f, arr[i+1]-5f, arr[i+2]+13f, arr[i+3]-5f, Color.BLACK);
                canvas.drawTriangle(Color.BLACK, arr[i+2]+8f, arr[i+3]-10f, arr[i+2]+13f, arr[i+3], arr[i+2]+18f, arr[i+3]-10f);
            }
        }
        if(finishedLevelNum != numOfLevels.get(currentContinent)){
            canvas.drawEllipse(Color.LIGHT_GRAY, arr[finishedLevelNum*2], arr[finishedLevelNum*2+1], nextLevel == finishedLevelNum ? 75f/2: 25f,
                    nextLevel == finishedLevelNum ? 45f/2: 15);
        }
    }

    public void drawCurrentContinent(){
        if(!zoomIn && !drawPoints && !zoomOut){
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
            if(!isMenuBPlaying)
            {
                menuBackground.loop(0.5f);
                isMenuBPlaying = true;
            }

            // We are are ready, notify our listener
            int hundred = 0;
            if(currentContinent == Continent.Europe) hundred = 1;
            if(currentContinent == Continent.Africa) hundred = 2;
            if(currentContinent == Continent.Oceania) hundred = 3;
            if(currentContinent == Continent.Asia) hundred = 4;
            if(currentContinent == Continent.NorthAmerica) hundred = 5;
            if(currentContinent == Continent.SouthAmerica) hundred = 6;
            if (isReady() && listener != null) {
                listener.updateScreen(this, hundred*10+nextLevel);
                menuSellect.play();
                menuBackground.stop();
                isMenuBPlaying = false;
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
                unlockedContinents.add(continent);
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
