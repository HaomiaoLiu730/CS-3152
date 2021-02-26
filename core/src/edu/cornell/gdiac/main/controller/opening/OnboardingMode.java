package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;

public class OnboardingMode implements ModeController, InputProcessor, ControllerListener, Loading {

    private final long FADING_TIME = 100;
    private final long FIRST_TEXT_TIME = 140;
    private final long SECOND_TEXT_TIME = 180;
    private final long THIRD_TEXT_TIME = 220;
    private final long FOURTH_TEXT_TIME = 250;
    private final float penguinY = 200;
    private final float PENGUIN_SCALE = 0.1f;

    /** is ready for game mode*/
    private boolean isReady = false;

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    /** Background texture for start-up */
    private Texture postcard;
    /** Black image */
    private Texture whiteTexture;
    /** penguin image*/
    private Texture roundPenguin;
    /** white line*/
    private Texture whiteLine;
    /** Cached Color attribute */
    private Color fadingColor;

    /** Standard width that the assets were designed for */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard height that the assets were designed for */
    private static int STANDARD_HEIGHT = 720;
    /** font */
    private BitmapFont letterFont;
    private BitmapFont gameFont;
    /** pause time*/
    long time = -160;
    private int FONT_SIZE = 40;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;
    private float penguinX;
    private float pengiunAngle;
    private Vector2 lineStart;
    private Vector2 lineEnd;

    private InputController inputController;

    public OnboardingMode(String file){
        // Waiting on these values until we see the canvas
        heightY = -1;
        scale = -1.0f;
        time = -200;
        penguinX = 100;

        // We need these files loaded immediately
        internal = new AssetDirectory( "onBoarding.json" );
        internal.loadAssets();
        internal.finishLoading();

        postcard = internal.getEntry( "postcard", Texture.class );
        whiteTexture = internal.getEntry("white", Texture.class);
        roundPenguin = internal.getEntry("roundPenguin", Texture.class);
        whiteLine = internal.getEntry("whiteLine", Texture.class);
        Gdx.input.setInputProcessor( this );
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MarkerFelt.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParam.size = FONT_SIZE;
        letterFont = generator.generateFont(fontParam);
        FreeTypeFontGenerator.FreeTypeFontParameter fontParam2 = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParam2.size = FONT_SIZE;
        fontParam2.color = Color.BLACK;
        gameFont = generator.generateFont(fontParam2);

        lineStart = new Vector2(penguinX,penguinY);
        lineEnd = new Vector2(penguinX, penguinY);

        // Start loading the real assets
        assets = new AssetDirectory( file );
        assets.loadAssets();
        fadingColor = new Color(0,0,0,1);

        inputController = InputController.getInstance();
    }

    @Override
    public void update() {
        inputController.readInput();
        if(inputController.didThrowPengiun()){
            isReady = true;
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        time += 1;
        pengiunAngle += 5f;
        pengiunAngle %= 360;
        penguinX += 2f;
        if(time < 0){
            canvas.drawOverlay(whiteTexture, true);
            canvas.drawText(gameFont,"Bear With Me", 400, 600);
            canvas.drawText(gameFont,"Team Octave", 500, 400);
            lineEnd.x = penguinX;
            canvas.drawLine(Color.BLACK, lineStart, lineEnd, 1);
            canvas.draw(roundPenguin,Color.WHITE,roundPenguin.getWidth()/2f,roundPenguin.getHeight()/2f, penguinX, penguinY, pengiunAngle,PENGUIN_SCALE,PENGUIN_SCALE);
        }else{
            canvas.drawOverlay(postcard, true);
            if(time > FADING_TIME){
                canvas.drawText(letterFont, "Hi my polar bear,", 730, 450);
            }
            if(time > FIRST_TEXT_TIME){
                canvas.drawText(letterFont, "I'm very sick right now", 730, 370);
            }
            if(time > SECOND_TEXT_TIME){
                canvas.drawText(letterFont, "and I really want to see you", 730, 290);
            }
            if(time > THIRD_TEXT_TIME){
                canvas.drawText(letterFont, "Pengiun", 730, 210);
            }
            if(time > FOURTH_TEXT_TIME){
                canvas.drawText(letterFont, "Press space to continue", 200, 350);
            }

            fadingColor.a = 1 - (float) time / FADING_TIME;
            if(fadingColor.a < 0){
                fadingColor.a = 0;}
            canvas.draw(whiteTexture, fadingColor, 0,0);
        }
    }



    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
        letterFont.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        heightY = height;
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
