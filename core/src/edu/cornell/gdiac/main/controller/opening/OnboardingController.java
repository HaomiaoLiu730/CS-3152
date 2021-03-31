package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class OnboardingController implements Screen, InputProcessor, ControllerListener, Loading {

    private final long FADING_TIME = 100;
    private final long FIRST_TEXT_TIME = 140;
    private final long SECOND_TEXT_TIME = 180;
    private final long THIRD_TEXT_TIME = 220;
    private final long FOURTH_TEXT_TIME = 250;
    private final float penguinY = 200;
    private final float PENGUIN_SCALE = 0.1f;

    /** is ready for game mode*/
    private boolean isReady = false;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    /** Front image */
    private Texture front;
    /** penguin image*/
    private Texture roundPenguin;

    /** Standard width that the assets were designed for */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard height that the assets were designed for */
    private static int STANDARD_HEIGHT = 720;
    /** font */
    private BitmapFont letterFont;
    private BitmapFont gameFont;
    /** pause time*/
    long time = -160;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;
    private float penguinX;
    private float pengiunAngle;
    private Vector2 lineStart;
    private Vector2 lineEnd;

    private InputController inputController;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    public OnboardingController(GameCanvas canvas, String file){
        // Waiting on these values until we see the canvas
        heightY = -1;
        scale = -1.0f;
        time = -200;
        penguinX = 100;
        active = false;
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory( "onBoarding.json" );
        internal.loadAssets();
        internal.finishLoading();

        front = internal.getEntry("front", Texture.class);
        roundPenguin = internal.getEntry("roundPenguin", Texture.class);
        Gdx.input.setInputProcessor( this );

        gameFont = internal.getEntry("gameFont", BitmapFont.class);
        letterFont = internal.getEntry("letterFont", BitmapFont.class);

        lineStart = new Vector2(penguinX,penguinY);
        lineEnd = new Vector2(penguinX, penguinY);

        // Start loading the real assets
        assets = new AssetDirectory( file );
        assets.loadAssets();

        inputController = InputController.getInstance();
        active = true;
    }

    public void update(float delta) {
        inputController.readInput();
        if(inputController.didThrowPengiun()){
            isReady = true;
        }
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public void draw() {
        canvas.begin();
        time += 1;
        pengiunAngle += 0.1f;
        pengiunAngle %= 360;
        penguinX += 2f;
        if(time < 0){
            canvas.drawOverlay(front, true);
            lineEnd.x = penguinX;
            canvas.drawLine(Color.BLACK, lineStart, lineEnd, 1);
            canvas.draw(roundPenguin,Color.WHITE,roundPenguin.getWidth()/2f,roundPenguin.getHeight()/2f, penguinX, penguinY, pengiunAngle,PENGUIN_SCALE,PENGUIN_SCALE);
        }else{
            canvas.drawOverlay(front, true);
            canvas.drawText(letterFont, "Press Space to Start", 485, 250);
        }
        canvas.end();
    }

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
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
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        heightY = height;
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

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        // hard-coded for now but will indicate whether the game is allowed to load in the future
        return isReady;
    }

    /**
     * Returns the asset directory produced by this loading screen
     *
     * This asset loader is NOT owned by this loading scene, so it persists even
     * after the scene is disposed.  It is your responsbility to unload the
     * assets in this directory.
     *
     * @return the asset directory produced by this loading screen
     */
    public AssetDirectory getAssets() {
        return assets;
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
}
