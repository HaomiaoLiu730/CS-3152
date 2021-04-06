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
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
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

        active  = true;
        zoomIn = true;
        currentContinent = Continent.NorthAmerica;
        camera = canvas.getCamera();
        this.canvas = canvas;
    }

    public void update(float delta) {
        if(zoomIn){
            // -620 -370 360 120 per 120
            switch (currentContinent){
                case NorthAmerica:
                    camera.viewportWidth = camera.viewportWidth >= 660 ? camera.viewportWidth-5.167f : camera.viewportWidth;
                    camera.viewportHeight = camera.viewportHeight >= 350 ? camera.viewportHeight-3.083f : camera.viewportHeight;
                    camera.position.x = camera.position.x <= 1000 ? camera.position.x + 3 :camera.position.x;
                    camera.position.y = camera.position.y <= 480 ? camera.position.y + 1 :camera.position.y;
                    camera.update();
                    if(camera.position.y == 480){
                        drawPoints = true;
                    }
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
        canvas.drawOverlay(northAmerica,0, 0);
        canvas.drawOverlay(southAmerica,grey,0,0);
        canvas.drawOverlay(oceania, grey,0, 0);
        canvas.drawOverlay(asia, grey,0, 0);
        canvas.drawOverlay(europe, grey,0, 0);
        canvas.drawOverlay(africa, grey,0, 0);
        canvas.drawOverlay(antarctica, grey,0, 0);
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
