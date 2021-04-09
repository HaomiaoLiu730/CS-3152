package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class MenuController extends ClickListener implements Screen, InputProcessor, ControllerListener, Loading {
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
    private enum Continent{
        NorthAmerica,
        SouthAmerica,
        Asia,
        Europe,
        Africa,
        Antarctica,
        Oceania
    }

    private float[] NORTH_AMERICA_LEVELS = new float[]{
            1100f, 600f, 1000f, 610f, 300f, 470f, 600f, 440f, 520f, 300f, 670f, 270f
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

    private BitmapFont gameFont;

    private int[] finishedLevels;
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

        background = internal.getEntry("background", Texture.class);
        northAmerica = internal.getEntry("NorthAmerica", Texture.class);
        southAmerica = internal.getEntry("SouthAmerica", Texture.class);
        asia = internal.getEntry("Asia", Texture.class);
        europe = internal.getEntry("Europe", Texture.class);
        antarctica = internal.getEntry("Antarctica", Texture.class);
        africa = internal.getEntry("Africa", Texture.class);
        oceania = internal.getEntry("Oceania", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);

        JsonValue levelProgress = internal.getEntry("finishedLevels", JsonValue.class);
        finishedLevels = levelProgress.get("finished").asIntArray();


        active  = true;
        zoomIn = true;
        currentContinent = Continent.NorthAmerica;
        camera = canvas.getCamera();
        this.canvas = canvas;
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
        if(zoomIn){
            switch (currentContinent){
                case NorthAmerica:
                    // -620 -370 360 120 per 120
                    zoomInto(660f, 350f, 1000f, 480f);
                    break;
                case SouthAmerica:
                    // -620 -370 360 120 per 120
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
        if(drawPoints){
            switch (currentContinent){
                case NorthAmerica:
                    for(int i = 0; i< NORTH_AMERICA_LEVELS.length; i+=2){
                        if (Math.abs(Gdx.input.getX() - NORTH_AMERICA_LEVELS[i]) <= MOUSE_TOL && Math.abs(720 - Gdx.input.getY() - NORTH_AMERICA_LEVELS[i+1]) <= MOUSE_TOL){
                            nextLevel = i;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        if(prevTouched && !Gdx.input.isTouched() && nextLevel != -1){
            isReady = true;
        }
        prevTouched = Gdx.input.isTouched();
    }

    public void draw() {
        canvas.begin();
        canvas.drawOverlay(background,true);
        canvas.drawOverlay(northAmerica, currentContinent == Continent.NorthAmerica ? Color.WHITE : grey, 0, 0);
        canvas.drawOverlay(southAmerica, currentContinent == Continent.SouthAmerica ? Color.WHITE : grey,0,0);
        canvas.drawOverlay(oceania, currentContinent == Continent.Oceania ? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(asia, currentContinent == Continent.Asia ? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(europe, currentContinent == Continent.Europe ? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(africa, currentContinent == Continent.Africa ? Color.WHITE : grey,0, 0);
        canvas.drawOverlay(antarctica, currentContinent == Continent.Antarctica ? Color.WHITE : grey,0, 0);
        if(drawPoints){
            switch (currentContinent){
                case NorthAmerica:
                    for(int i = 0; i< NORTH_AMERICA_LEVELS.length; i+=2){
                        canvas.drawCircle(Color.LIGHT_GRAY, NORTH_AMERICA_LEVELS[i], NORTH_AMERICA_LEVELS[i+1], nextLevel == i ? 10f: 5f);
                    }
                    break;
                default:
                    break;
            }
        }
        canvas.end();
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
            if (isReady() && listener != null) {
                listener.updateScreen(this, 0);
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
