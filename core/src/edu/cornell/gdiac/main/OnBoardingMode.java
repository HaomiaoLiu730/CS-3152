package edu.cornell.gdiac.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.XBoxController;

public class OnBoardingMode implements ModeController, InputProcessor, ControllerListener {

    private final long FADING_TIME = 100;
    private final long FIRST_TEXT_TIME = 170;
    private final long SECOND_TEXT_TIME = 240;
    private final long THIRD_TEXT_TIME = 300;

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    /** Background texture for start-up */
    private Texture postcard;
    /** Black image */
    private Texture whiteTexture;
    /** Cached Color attribute */
    private Color fadingColor;

    /** Standard width that the assets were designed for */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard height that the assets were designed for */
    private static int STANDARD_HEIGHT = 720;
    /** font generator */
    FreeTypeFontGenerator generator;
    /** pause time*/
    long time = 0;

    private int FONT_SIZE = 40;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    public OnBoardingMode(String file){
        // Waiting on these values until we see the canvas
        heightY = -1;
        scale = -1.0f;
        time = 0;

        // We need these files loaded immediately
        internal = new AssetDirectory( "onBoarding.json" );
        internal.loadAssets();
        internal.finishLoading();

        postcard = internal.getEntry( "postcard", Texture.class );
        whiteTexture = internal.getEntry("white", Texture.class);
        Gdx.input.setInputProcessor( this );
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MarkerFelt.ttf"));

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener( this );
        }

        // Start loading the real assets
        assets = new AssetDirectory( file );
        assets.loadAssets();
        fadingColor = new Color(0,0,0,1);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(GameCanvas canvas) {
        time += 1;
        // If this is the first time drawing, get info from the canvas.
        canvas.drawOverlay(postcard, true);
        FreeTypeFontGenerator.FreeTypeFontParameter fontParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParam.size = FONT_SIZE;
        BitmapFont font = generator.generateFont(fontParam);
        if(time > FADING_TIME){
            canvas.drawText(font, "Hi my polar bear,", 730, 450);
        }
        if(time > FIRST_TEXT_TIME){
            canvas.drawText(font, "I'm very sick right now", 730, 370);
        }
        if(time > SECOND_TEXT_TIME){
            canvas.drawText(font, "and I really want to see you", 730, 290);
        }
        if(time > THIRD_TEXT_TIME){
            canvas.drawText(font, "Pengiun", 730, 210);
        }

        fadingColor.a = 1 - (float) time / FADING_TIME;
        if(fadingColor.a < 0){
            fadingColor.a = 0;}
        canvas.draw(whiteTexture, fadingColor, 0,0);
    }

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
        generator.dispose();
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
        return false;
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
