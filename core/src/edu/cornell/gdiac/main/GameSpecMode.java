package edu.cornell.gdiac.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Texture;
import edu.cornell.gdiac.assets.AssetDirectory;

public class GameSpecMode implements ModeController, InputProcessor, ControllerListener,  Loading{
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** The background image for the spec */
    private Texture background;

    public GameSpecMode(float width, float height) {
        // Extract the assets from the asset directory.  All images are textures.
        internal = new AssetDirectory( "gameSpecs.json" );
        internal.loadAssets();
        internal.finishLoading();

        background = internal.getEntry("background", Texture.class );
        Gdx.input.setInputProcessor( this );
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.drawOverlay(background, true);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public AssetDirectory getAssets() {
        return null;
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
