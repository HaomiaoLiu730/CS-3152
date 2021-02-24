package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;

public class GameSpecMode implements ModeController, InputProcessor, ControllerListener, Loading {
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** The background image for the spec */
    private Texture background;
    private BitmapFont mainFont;
    private BitmapFont promptFont;
    private float textHeight = -5f;
    private InputController inputController;
    private boolean isReady;
    private AssetDirectory asset;

    private static final float Y_INCREMENT = 1;
    private static final int FONT_SIZE = 30;
    private static final int PROMPT_FONT_SIZE = 20;
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

    public GameSpecMode(float width, float height) {
        // Extract the assets from the asset directory.  All images are textures.
        internal = new AssetDirectory( "gameSpecs.json" );
        internal.loadAssets();
        internal.finishLoading();

        background = internal.getEntry("white", Texture.class );
        Gdx.input.setInputProcessor( this );

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MarkerFelt.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParam.size = FONT_SIZE;
        mainFont = generator.generateFont(fontParam);
        FreeTypeFontGenerator.FreeTypeFontParameter promptFontParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        promptFontParam.size = PROMPT_FONT_SIZE;
        promptFont = generator.generateFont(promptFontParam);

        isReady = false;
        inputController = new InputController();

        asset = new AssetDirectory("assets.json");
        asset.loadAssets();
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
        canvas.drawOverlay(background, Color.BLACK, true);
        textHeight += Y_INCREMENT;
        canvas.drawText(mainFont, info, 120, textHeight);
        canvas.drawText(promptFont, "Press space to skip", 1100, 700);
    }

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
        mainFont.dispose();
        promptFont.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public AssetDirectory getAssets() {
        return asset;
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
