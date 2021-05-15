package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.GDXRoot;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.controller.gaming.GameplayController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class OnboardingController implements Screen, InputProcessor, ControllerListener, Loading {

    private final float penguinY = 200;
    private int flag = 0;

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
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;
    private InputController inputController;
    private boolean isHoverPlay=false;
    private boolean isHoverLevel=false;


    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    public OnboardingController(GameCanvas canvas, String file){
        // Waiting on these values until we see the canvas
        heightY = -1;
        scale = -1.0f;
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

        // Start loading the real assets
        assets = new AssetDirectory( file );
        assets.loadAssets();

        inputController = InputController.getInstance();
        active = true;
    }

    public void update(float delta) {
        if (flag == 0) {
            flag ++;
        } else if (flag == 1) {
            GDXRoot.load1();
            flag ++;
        } else if (flag == 2) {
            GDXRoot.load2();
            flag ++;
        } else if (flag == 3) {
            GDXRoot.load3();
            flag ++;
        }
        inputController.readInput();
        if(Math.abs(Gdx.input.getX() - 645) <= 95 && Math.abs(720 - Gdx.input.getY() - 340) <= 35){
            isHoverLevel=true;
            if (InputController.getInstance().touchUp())
                listener.updateScreen(this, 0);
        }
        else if(Math.abs(Gdx.input.getX() - 660) <= 50 && Math.abs(720 - Gdx.input.getY() - 408) <= 20){
            isHoverPlay=true;
            if (InputController.getInstance().touchUp())
                listener.updateScreen(this, 2);

        }
        else{
            isHoverLevel=false;
            isHoverPlay=false;
        }
        if(inputController.didPressE()){
            listener.updateScreen(this, 1);
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

    public void drawStart(float scale_play, float scale_level, int x_play, int x_level){
        letterFont.getData().setScale(scale_play);
        canvas.drawText(letterFont, "Play", x_play, 435);
        letterFont.getData().setScale(scale_level);
        canvas.drawText(letterFont, "Level Select", x_level, 350);

    }
    public void draw() {
        canvas.begin();
        canvas.drawOverlay(front, true);
        if (flag == 1) {
            canvas.drawText(letterFont, "Loading .", 550, 400);
        } else if (flag == 2) {
            canvas.drawText(letterFont, "Loading ..", 550, 400);
        } else if (flag == 3) {
            canvas.drawText(letterFont, "Loading ...", 550, 400);
        } else if (flag == 4) {
            if(!isHoverLevel&&!isHoverPlay) {
                drawStart(0.95f,0.95f,615,557);
            }
            else if (isHoverLevel){
                drawStart(0.95f,1f,615,555);
            }
            else if(isHoverPlay){
                drawStart(1f,0.95f,614,557);
            }
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
