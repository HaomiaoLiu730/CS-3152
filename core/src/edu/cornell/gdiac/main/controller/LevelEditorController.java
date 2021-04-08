package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;
import java.util.Arrays;

public class LevelEditorController implements Screen, InputProcessor, ControllerListener, Loading {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** is ready for game mode*/
    private boolean isReady = false;
    /** Whether or not this player mode is still active */
    private boolean active;
    private InputController inputController = InputController.getInstance();

    private static final int WIDTH = 320;
    private static final int HEIGHT = 18;

    private static final float ICICLE_X = 340;
    private static final float ICEBAR_X = 80;
    private static final float NOTE_X = 20;
    private static final float TARGET_X = 680f;
    private static final float TARGET_Y = 640f;

    private static final ArrayList<Component> TEXTURE_COMPONENTS = new ArrayList<>();
    private static final ArrayList<Component> POLYGON_COMPONENTS = new ArrayList<>();

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
    protected FilmStrip water;
    /** The texture for the ice */
    protected FilmStrip iceStrip;
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
    protected PolygonRegion icicleRegion;
    /** The texture for the notes not collected */
    protected FilmStrip noteLeftStrip;
    /** The texture for the notes collected */
    protected FilmStrip noteCollectedStrip;
    /** The texture for the exit */
    protected FilmStrip exitStrip;
    protected TextureRegion snow;
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;

    private float icicleWidth = 80;
    private float iceBarWidth = 200f;

    private int currentWidth;
    private int currentHeight;
    private FilmStrip currentStrip;
    private PolygonRegion currentPolygonRegion;

    private ArrayList<GenericComponent> objects = new ArrayList<>();

    private Tile[] tiles= new Tile[WIDTH];
    private int[] height = new int[WIDTH];
    private Component currentComponent;
    private Vector2 posCache = new Vector2();
    private boolean isDragging;
    private GenericComponent draggingObj;

    private Vector2 posCache1 = new Vector2();
    private Vector2 posCache2 = new Vector2();
    private Vector2 posCache3 = new Vector2();
    private Vector2 posCache4 = new Vector2();

    private enum Tile{
        Snow,
        Water,
        Air,
    }

    private enum Component{
        Note,
        IceBar,
        Icicle
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
        exitStrip = new FilmStrip(internal.getEntry("exit", Texture.class), 1, 1);
        arrowTexture = internal.getEntry("arrow", Texture.class);
        energyBarOutlineTexture = internal.getEntry("energyBarOutline", Texture.class);
        energyBarTexture = internal.getEntry("energyBar", Texture.class);
        noteLeftStrip = new FilmStrip(internal.getEntry("notcollected", Texture.class), 1, 1);
        noteCollectedStrip = new FilmStrip(internal.getEntry("collected", Texture.class), 1, 1);
        icicleStrip = new FilmStrip(internal.getEntry("icicle", Texture.class), 1, 1);
        float[] vertices = {-40f,40f,40f,40f,0,-80f};
        short[] triangles = {0,1,2};
        icicleRegion = new PolygonRegion(icicleStrip, vertices,triangles);

        water = new FilmStrip(internal.getEntry("water", Texture.class), 1,1);
        water.setRegionWidth(40);
        water.setRegionHeight(40);
        iceStrip= new FilmStrip(internal.getEntry("ice", Texture.class), 1, 1);
        iceStrip.setRegionWidth(200);
        iceStrip.setRegionHeight(40);
        displayFont =  internal.getEntry("gameFont", BitmapFont.class);
        Arrays.fill(tiles, Tile.Air);
        Arrays.fill(height, -1);

        TEXTURE_COMPONENTS.add(Component.Note);
        TEXTURE_COMPONENTS.add(Component.IceBar);
        POLYGON_COMPONENTS.add(Component.Icicle);
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
        float x = inputController.getClickX();
        float y = inputController.getClickY();
        createComponents(x,y);
        positionComponents(x,y);
    }

    public void positionComponents(float x, float y){
        if(!inputController.getPrevIsTouching() && Gdx.input.isTouched()){
            for(GenericComponent obj: objects){
                float posX = obj.position.x;
                float posY = obj.position.y;
                if(TEXTURE_COMPONENTS.contains(obj.component)){
                    float objWidth = obj.filmStrip.getRegionWidth();
                    float objHeight = obj.filmStrip.getRegionHeight();
                    if(x > posX && x < posX + objWidth && 720-y > posY && 720-y < posY + objHeight){
                        isDragging = true;
                        draggingObj = obj;
                    }
                }else{
                    posCache1.set(x,720 - y);
                    posCache2.set(obj.polygonRegion.getVertices()[0]+obj.position.x, obj.polygonRegion.getVertices()[1]+obj.position.y);
                    posCache3.set(obj.polygonRegion.getVertices()[2]+obj.position.x, obj.polygonRegion.getVertices()[3]+obj.position.y);
                    posCache4.set(obj.polygonRegion.getVertices()[4]+obj.position.x, obj.polygonRegion.getVertices()[4]+obj.position.y);
                    if(pointInTriangle(posCache1, posCache2, posCache3, posCache4)){
                        isDragging = true;
                        draggingObj = obj;
                    }
                }
            }
        }
        if(isDragging){
            if(TEXTURE_COMPONENTS.contains(draggingObj.component)){
                draggingObj.position.set(x-draggingObj.filmStrip.getRegionWidth()/2f, 720-y-draggingObj.filmStrip.getRegionHeight()/2f);
            }else{
                draggingObj.position.set(x, 720-y);
            }
        }
        if(isDragging && !Gdx.input.isTouched()){
            isDragging = false;
            float posX = ((int)draggingObj.position.x/4)*4;
            float posY = ((int)(draggingObj.position.y)/4)*4;
            draggingObj.position.set(posX,posY);
        }
    }

    public void createComponents(float x, float y){
        if(inputController.didTouchUp() && !isDragging){
            if(y > 320){
                formTile(x,y);
            }else{
                updateCurrentComponent(x,y);
                updateSize(x, y);
                addComponent(x,y);
            }
        }

    }

    public static float sign (Vector2 p1, Vector2 p2, Vector2 p3)
    {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    // using barycentric coordinates to tell whether a point is inside a triangle
    public boolean pointInTriangle (Vector2 pt, Vector2 v1, Vector2 v2, Vector2 v3)
    {
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    public void addComponent(float x, float y){
        if(x > 1160 && x < 1240 && y > 60 && y < 100){
            if(currentComponent != null){
                switch (currentComponent){
                    case Note:
                        objects.add(new GenericComponent(currentStrip, Component.Note));
                        break;
                    case IceBar:
                        objects.add(new GenericComponent(currentStrip, Component.IceBar));
                        break;
                    case Icicle:
                        objects.add(new GenericComponent(currentPolygonRegion, Component.Icicle));
                        break;
                }
                currentComponent = null;
            }
        }
    }

    public void formTile(float x, float y){
        int i = (int)(x/40f);
        int j = (int)((720 - y)/40f);
        if(height[i] == j){
            tiles[i] = tiles[i] == Tile.Air ? Tile.Snow:(tiles[i] == Tile.Snow ? Tile.Water : Tile.Air);
        }else{
            height[i] = j;
            tiles[i] = Tile.Snow;
        }
    }

    public void updateSize(float x, float y){
        if(posCache.set(x, y).sub(1060, 720-680).len() < 16){
            currentWidth += 4;
        }else if(posCache.set(x, y).sub(1060, 720-620).len() <16){
            currentWidth -= 4;
        }else if(posCache.set(x, y).sub(1120, 720-680).len() <16){
            currentHeight += 4;
        }else if(posCache.set(x, y).sub(1120, 720-620).len() <16){
            currentHeight -= 4;
        }
    }

    public void updateCurrentComponent(float x, float y){
        if(y < 320){
            if(x - NOTE_X <= 40){
                currentComponent = Component.Note;
                currentStrip = noteLeftStrip.copy();
                currentWidth = noteLeftStrip.getRegionWidth();
                currentHeight = noteLeftStrip.getRegionHeight();
            }else if(x - ICEBAR_X <= iceBarWidth){
                currentComponent = Component.IceBar;
                currentStrip = iceStrip.copy();
                currentWidth = iceStrip.getRegionWidth();
                currentHeight = iceStrip.getRegionHeight();
            }else if(x - ICICLE_X <= icicleWidth){
                currentComponent = Component.Icicle;
                currentPolygonRegion = new PolygonRegion(icicleStrip, icicleRegion.getVertices().clone(), icicleRegion.getTriangles());
                currentWidth = 80;
                currentHeight = 80;
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
        drawPanel();
        drawTarget();
        drawComponents();
        canvas.end();
    }

    public void drawComponents(){
        for(GenericComponent obj: objects){
            if(TEXTURE_COMPONENTS.contains(obj.component)){
                canvas.draw(obj.filmStrip, obj.position.x, obj.position.y);
            }else if(POLYGON_COMPONENTS.contains(obj.component)){
                canvas.draw(obj.polygonRegion, obj.position.x, obj.position.y);
            }
        }
    }

    public void drawTarget(){
        if(currentComponent != null && TEXTURE_COMPONENTS.contains(currentComponent)){
            currentStrip.setRegionWidth(currentWidth);
            currentStrip.setRegionHeight(currentHeight);
            canvas.draw(currentStrip, TARGET_X, TARGET_Y);
        }else if(currentComponent != null && POLYGON_COMPONENTS.contains(currentComponent)){
            currentPolygonRegion.getVertices()[0] = -currentWidth/2f;
            currentPolygonRegion.getVertices()[2] = currentWidth/2f;
            currentPolygonRegion.getVertices()[5] = -currentHeight;
            canvas.draw(currentPolygonRegion, TARGET_X, TARGET_Y);
        }
    }

    public void drawGrid(){
        for (int i = 0; i < WIDTH; i++) {
            // draw vertical
            float x = 12800/WIDTH*i;
            canvas.drawLine(Color.WHITE,x, 0, x, 720, 1);
        }
        for (int i = 0; i < HEIGHT; i++) {
            // draw horizontal
            float y = 720/HEIGHT*i;
            canvas.drawLine(Color.WHITE,0, y, 12800, y, 1);
        }
        for(int i = 0; i<WIDTH; i++){
            for(int j = 0; j<=height[i]; j++){
                if(tiles[i] != Tile.Air){
                    canvas.draw(tiles[i] == Tile.Snow? snow:water,40f*i,40f*j);
                }
            }
        }
    }

    public void drawPanel(){
        canvas.draw(noteLeftStrip,NOTE_X,640);
        canvas.draw(iceStrip,ICEBAR_X,640);
        canvas.draw(icicleRegion,ICICLE_X,640);
        canvas.drawText(displayFont, "width", 1040, 660);
        canvas.drawText(displayFont, "height", 1100, 660);
        canvas.drawCircle(Color.BLACK, 1060, 680, 6);
        canvas.drawCircle(Color.BLACK, 1060, 620, 6);
        canvas.drawCircle(Color.BLACK, 1120, 680, 6);
        canvas.drawCircle(Color.BLACK, 1120, 620, 6);
        canvas.drawText(displayFont, currentComponent == null ? "" : currentComponent.name(), 1180, 700);
        canvas.drawSquare(Color.BLACK, 1160, 620, 80, 40);
        canvas.drawText(displayFont, "add", 1200, 640);
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


    protected class GenericComponent{
        public Vector2 position;
        public FilmStrip filmStrip;
        public PolygonRegion polygonRegion;
        public String name;
        public Component component;
        public GenericComponent(FilmStrip filmStrip, Component component){
            this.filmStrip = filmStrip;
            this.position = new Vector2(TARGET_X, TARGET_Y);
            this.component = component;
        }
        public GenericComponent(PolygonRegion polygonRegion, Component component){
            this.polygonRegion = polygonRegion;
            this.position = new Vector2(TARGET_X, TARGET_Y);
            this.component = component;
        }
    }


}
