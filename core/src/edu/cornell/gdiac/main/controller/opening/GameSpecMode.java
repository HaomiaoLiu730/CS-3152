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
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class GameSpecMode implements Screen, InputProcessor, ControllerListener, Loading {
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** The background image for the spec */
    private Texture background;
    private BitmapFont mainFont;
    private BitmapFont promptFont;
    private float textHeight = -5f;
    private InputController inputController;
    private boolean isReady;
    private AssetDirectory asset;

    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Standard width that the assets were designed for */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard height that the assets were designed for */
    private static int STANDARD_HEIGHT = 720;
    private static final float Y_INCREMENT = 1;
    private static final String info =
            "When the polar bear was a child, he found a drifting bottle on the beach\n" +
            "with a penguin, Lay\'s letter in it.\n"+
            "Since then, they have become pen pals sending letters to each other.\n"+
            "Lay is a little older than him. He is the leader of an Akabella choir\n"+
            "and often sends some tapes of his works to the polar bear.\n"+
            "The polar bear admires him very much because all polar bears are tone-deaf.\n"+
            "He has never heard such beautiful music.\n"+
            "He hoped that he could listen to the complete A cappella chorus one day, \n"+
            "but Lay told him that the zoo had captured all members except for him.\n"+
            "Humans have taken a fancy to these penguins\'s ability to sing and hope to capture them\n"+
            "for experimentation and use them to make the world\'s most beautiful sounds.\n"+
            "Only he escaped because he got albinism. The polar bear wanted to help Lay\'s friends, \n"+
            "but his parents did not allow him to leave the North Pole because he was too young\n"+
            "A few years later, he finally became an adult, and Lay\'s illness became more serious\n"+
            "The brave polar bear is about to start on a journey to find the penguins alone.\n";

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    public GameSpecMode(GameCanvas canvas,  String file) {
        // Extract the assets from the asset directory.  All images are textures.

        internal = new AssetDirectory(file);
        internal.loadAssets();
        internal.finishLoading();
        this.canvas = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        background = internal.getEntry("white", Texture.class );
        Gdx.input.setInputProcessor( this );

        mainFont = internal.getEntry("mainFont", BitmapFont.class);
        promptFont = internal.getEntry("promptFont", BitmapFont.class);

        isReady = false;
        inputController = new InputController();

        asset = new AssetDirectory("assets.json");
        asset.loadAssets();
        active = true;
    }

    public void update(float delta) {
        inputController.readInput();
        if(inputController.didThrowPengiun()){
            isReady = true;
        }
    }

    public void draw() {
        canvas.begin();
        canvas.drawOverlay(background, Color.BLACK, true);
        textHeight += Y_INCREMENT;
        canvas.drawText(mainFont, info, 120, textHeight);
        canvas.drawText(promptFont, "Press space to skip", 1100, 700);
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
            if (isReady && listener != null) {
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

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public AssetDirectory getAssets() {
        return asset;
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
