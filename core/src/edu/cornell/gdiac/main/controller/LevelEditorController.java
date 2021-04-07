package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.Arrays;

public class LevelEditorController implements Screen, InputProcessor, ControllerListener, Loading {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** is ready for game mode*/
    private boolean isReady = false;
    /** Whether or not this player mode is still active */
    private boolean active;
    private InputController inputController = InputController.getInstance();

    private static final int WIDTH = 32;
    private static final int HEIGHT = 18;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    private Color gray = new Color(0.92f,0.92f,0.92f,1f);

    /** The texture for the player */
    protected Texture white;
    /** The texture for the player */
    protected FilmStrip avatarStrip;
    /** The texture for the penguin */
    protected FilmStrip penguinWalkingStrip;
    /** The texture for the penguin rolling */
    protected FilmStrip penguinRollingStrip;
    /** The texture for the monster */
    protected FilmStrip monsterStrip;
    /** The texture for the water */
    protected TextureRegion water;
    /** The texture for the ice */
    protected TextureRegion iceStrip;
    /** The texture for the monster attacking */
    protected FilmStrip attackStrip;
    /** The texture for the arrow */
    protected Texture arrowTexture;
    /** The texture for the energy bar */
    protected Texture energyBarTexture;
    /** The texture for the energy bar */
    protected Texture energyBarOutlineTexture;
    /** The texture for the icicle */
    protected FilmStrip icicleStrip;
    /** The texture for the notes not collected */
    protected FilmStrip noteLeftStrip;
    /** The texture for the notes collected */
    protected FilmStrip noteCollectedStrip;
    /** The texture for the exit */
    protected FilmStrip exitStrip;
    protected TextureRegion snow;
    private Tile[] tiles= new Tile[WIDTH];
    private int[] height = new int[WIDTH];

    private enum Tile{
        Snow,
        Water,
        Air,
    }

    public LevelEditorController(GameCanvas canvas){
        this.canvas = canvas;
        // We need these files loaded immediately
        internal = new AssetDirectory( "assets.json" );
        internal.loadAssets();
        internal.finishLoading();
        white = internal.getEntry("white", Texture.class);
        snow = new TextureRegion(internal.getEntry("snow", Texture.class),40,40);
        avatarStrip = new FilmStrip(internal.getEntry("avatarWalking", Texture.class), 1, 18);
        penguinWalkingStrip = new FilmStrip(internal.getEntry("penguinWalking", Texture.class), 1, 29);
        penguinRollingStrip = new FilmStrip(internal.getEntry("penguinRolling", Texture.class), 1, 1);
        monsterStrip = new FilmStrip(internal.getEntry("monster", Texture.class), 1, 1);
        attackStrip = new FilmStrip(internal.getEntry("monsterAttacking", Texture.class), 1, 5);
        icicleStrip = new FilmStrip(internal.getEntry("icicle", Texture.class), 1, 1);
        exitStrip = new FilmStrip(internal.getEntry("exit", Texture.class), 1, 1);
        arrowTexture = internal.getEntry("arrow", Texture.class);
        energyBarOutlineTexture = internal.getEntry("energyBarOutline", Texture.class);
        energyBarTexture = internal.getEntry("energyBar", Texture.class);
        noteLeftStrip = new FilmStrip(internal.getEntry("notcollected", Texture.class), 1, 1);
        noteCollectedStrip = new FilmStrip(internal.getEntry("collected", Texture.class), 1, 1);
        icicleStrip = new FilmStrip(internal.getEntry("icicle", Texture.class), 1, 1);
        water = new TextureRegion(internal.getEntry("water", Texture.class), 40, 40);
        iceStrip= new TextureRegion(internal.getEntry("ice", Texture.class), 200, 40);
        Arrays.fill(tiles, Tile.Air);
        Arrays.fill(height, -1);
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
    public boolean isReady() {
        return false;
    }

    @Override
    public AssetDirectory getAssets() {
        return null;
    }

    @Override
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    @Override
    public void show() {
        active = true;
    }

    public void update(float delta) {
        inputController.readInput();
        if(inputController.didTouchUp()){
            int i = (int)(inputController.getClickX()/40f);
            int j = (int)((720 - inputController.getClickY())/40f);
            if(height[i] == j){
                tiles[i] = tiles[i] == Tile.Air ? Tile.Snow:(tiles[i] == Tile.Snow ? Tile.Water : Tile.Air);
            }else{
                height[i] = j;
                tiles[i] = Tile.Snow;
            }
        }
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
        }
    }

    public void draw() {
        canvas.begin();
        canvas.drawOverlay(white, gray, true);
        drawGrid();
        drawComponents();
        canvas.end();
    }

    public void drawGrid(){
        for (int i = 0; i < WIDTH; i++) {
            // draw vertical
            float x = 1280/WIDTH*i;
            canvas.drawLine(Color.WHITE,x, 0, x, 720, 1);
        }
        for (int i = 0; i < HEIGHT; i++) {
            // draw horizontal
            float y = 720/HEIGHT*i;
            canvas.drawLine(Color.WHITE,0, y, 1280, y, 1);
        }
        for(int i = 0; i<WIDTH; i++){
            for(int j = 0; j<=height[i]; j++){
                if(tiles[i] != Tile.Air){
                    canvas.draw(tiles[i] == Tile.Snow? snow:water,40f*i,40f*j);
                }
            }
        }
    }

    public void drawComponents(){
        canvas.draw(noteCollectedStrip,20,660);
        canvas.draw(iceStrip,80,660);
        canvas.draw(icicleStrip,320,660);
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

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
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
