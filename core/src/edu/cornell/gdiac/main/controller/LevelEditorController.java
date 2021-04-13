package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.GDXRoot;
import edu.cornell.gdiac.main.controller.opening.Loading;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

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

    private static final float EXIT_X = 500;
    private static final float MONSTER_X = 360;
    private static final float ICICLE_X = 260;
    private static final float ICE_X = 80;
    private static final float FLOATING_ICE_X = 160;
    private static final float MOVING_ICE_X = 240;
    private static final float NOTE_X = 20;
    private static float target_x = 680f;
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
    protected FilmStrip monsterVerStrip;
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

    private Tile[] tilesBottom = new Tile[WIDTH*10];
    private Tile[] tilesTop = new Tile[WIDTH*10];
    private int[] heightBottom = new int[WIDTH*10];
    private int[] heightTop = new int[WIDTH*10];
    private Component currentComponent;
    private GenericComponent draggingComponent;
    private Vector2 posCache = new Vector2();
    private boolean isDragging;
    private GenericComponent draggingObj;

    private Vector2 posCache1 = new Vector2();
    private Vector2 posCache2 = new Vector2();
    private Vector2 posCache3 = new Vector2();
    private Vector2 posCache4 = new Vector2();

    private LevelJson levelJson = new LevelJson();

    private float cameraOffset;

    private enum Tile{
        Snow,
        Water,
        Air,
    }

    private enum Component{
        Note,
        Ice,
        FloatingIce,
        MovingIce,
        Icicle,
        MonsterHori,
        MonsterVer,
        Exit,
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
        monsterStrip = new FilmStrip(internal.getEntry("monsterScaled", Texture.class), 1, 1);
        monsterVerStrip = new FilmStrip(internal.getEntry("monsterScaledVer", Texture.class), 1, 1);
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
        Arrays.fill(tilesBottom, Tile.Air);
        Arrays.fill(heightBottom, -1);
        Arrays.fill(tilesTop, Tile.Air);
        Arrays.fill(heightTop, -1);
        TEXTURE_COMPONENTS.add(Component.Note);
        TEXTURE_COMPONENTS.add(Component.Ice);
        TEXTURE_COMPONENTS.add(Component.FloatingIce);
        TEXTURE_COMPONENTS.add(Component.MovingIce);
        TEXTURE_COMPONENTS.add(Component.Exit);
        TEXTURE_COMPONENTS.add(Component.MonsterHori);
        TEXTURE_COMPONENTS.add(Component.MonsterVer);
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
        cameraOffset = canvas.getCamera().position.x-640;
        target_x = TARGET_X + cameraOffset;
        float x = inputController.getClickX();
        float y = inputController.getClickY();
        createComponents(x,y);
        positionComponents(x,y);
        deleteComponent(x,y);
        finishLevel(x,y);
        moveCamera();
    }

    public void moveCamera(){
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            canvas.getCamera().position.x += 40;
        }else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            canvas.getCamera().position.x = Math.max(canvas.getCamera().position.x-20, 640);
        }
        canvas.getCamera().update();
    }

    public void positionComponents(float x, float y){
        x += cameraOffset;
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
                        draggingComponent = obj;
                        break;
                    }
                }else{
                    posCache1.set(x,720 - y);
                    posCache2.set(obj.polygonRegion.getVertices()[0]+obj.position.x, obj.polygonRegion.getVertices()[1]+obj.position.y);
                    posCache3.set(obj.polygonRegion.getVertices()[2]+obj.position.x, obj.polygonRegion.getVertices()[3]+obj.position.y);
                    posCache4.set(obj.polygonRegion.getVertices()[4]+obj.position.x, obj.polygonRegion.getVertices()[4]+obj.position.y);
                    if(pointInTriangle(posCache1, posCache2, posCache3, posCache4)){
                        isDragging = true;
                        draggingObj = obj;
                        draggingComponent = obj;
                        break;
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
            float posX = ((int)draggingObj.position.x/8)*8;
            float posY = ((int)(draggingObj.position.y)/8)*8;
            draggingObj.position.set(posX,posY);
        }
    }

    public void createComponents(float x, float y){
        if(inputController.didTouchUp() && !isDragging){
            if(y > 360){
                formTileBottom(x,y);
            }else if(y > 80 && x < 1040){
                formTileTop(x,y);
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
        if(x > 1160 && x < 1220 && y > 50 && y < 80){
            if(currentComponent != null){
                switch (currentComponent){
                    case Note:
                        objects.add(new GenericComponent(currentStrip, Component.Note));
                        break;
                    case Ice:
                        objects.add(new GenericComponent(currentStrip, Component.Ice));
                        break;
                    case FloatingIce:
                        objects.add(new GenericComponent(currentStrip, Component.FloatingIce));
                        break;
                    case MovingIce:
                        objects.add(new GenericComponent(currentStrip, Component.MovingIce));
                        break;
                    case Icicle:
                        objects.add(new GenericComponent(currentPolygonRegion, Component.Icicle));
                        break;
                    case MonsterHori:
                        objects.add(new GenericComponent(currentStrip, Component.MonsterHori));
                        break;
                    case MonsterVer:
                        objects.add(new GenericComponent(currentStrip, Component.MonsterVer));
                        break;
                    case Exit:
                        objects.add(new GenericComponent(currentStrip, Component.Exit));
                        break;
                }
                currentComponent = null;
            }
        }
    }

    public void deleteComponent(float x, float y){
        if(x > 1160 && x < 1220 && y > 90 && y < 120 && inputController.didTouchUp()){
            if(draggingComponent != null){
                objects.remove(draggingComponent);
                draggingComponent = null;
            }
        }
    }

    public void finishLevel(float x, float y){
        if(x > 1160 && x < 1220 && y > 130 && y < 160 && inputController.didTouchUp()){
            generateJson();
        }
    }

    public void formTileBottom(float x, float y){
        x += cameraOffset;
        int i = (int)(x/40f);
        int j = (int)((720 - y)/40f);
        if(heightBottom[i] == j){
            tilesBottom[i] = tilesBottom[i] == Tile.Air ? Tile.Snow:(tilesBottom[i] == Tile.Snow ? Tile.Water : Tile.Air);
        }else{
            heightBottom[i] = j;
            tilesBottom[i] = Tile.Snow;
        }
    }

    public void formTileTop(float x, float y){
        x += cameraOffset;
        int i = (int)(x/40f);
        int j = (int)(y/40f);
        if(heightTop[i] == j+1){
            tilesTop[i] = tilesTop[i] == Tile.Air ? Tile.Snow:Tile.Air;
        }else{
            heightTop[i] = j+1;
            tilesTop[i] = Tile.Snow;
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
        if(y <80){
            int grid = (int) (x/40);
            if(grid == 0){
                currentComponent = Component.Note;
                currentStrip = noteLeftStrip.copy();
                currentWidth = noteLeftStrip.getRegionWidth();
                currentHeight = noteLeftStrip.getRegionHeight();
            }else if(grid == 1 || grid == 2){
                currentComponent = Component.Ice;
                currentStrip = iceStrip.copy();
                currentWidth = iceStrip.getRegionWidth();
                currentHeight = iceStrip.getRegionHeight();
            }else if(grid == 3 || grid == 4){
                currentComponent = Component.FloatingIce;
                currentStrip = iceStrip.copy();
                currentWidth = iceStrip.getRegionWidth();
                currentHeight = iceStrip.getRegionHeight();
            }else if(grid == 5 || grid == 6){
                currentComponent = Component.MovingIce;
                currentStrip = iceStrip.copy();
                currentWidth = iceStrip.getRegionWidth();
                currentHeight = iceStrip.getRegionHeight();
            }
            else if(grid == 7){
                currentComponent = Component.Icicle;
                currentPolygonRegion = new PolygonRegion(icicleStrip, icicleRegion.getVertices().clone(), icicleRegion.getTriangles());
                currentWidth = 80;
                currentHeight = 80;
            }else if(grid == 8 || grid == 9){
                currentComponent = Component.MonsterHori;
                currentStrip = monsterStrip.copy();
                currentWidth = monsterStrip.getRegionWidth();
                currentHeight = monsterStrip.getRegionHeight();
            } else if(grid == 10 || grid == 11){
                currentComponent = Component.MonsterVer;
                currentStrip = monsterVerStrip.copy();
                currentWidth = monsterVerStrip.getRegionWidth();
                currentHeight = monsterVerStrip.getRegionHeight();
            }else if(grid == 12){
                currentComponent = Component.Exit;
                currentStrip = exitStrip.copy();
                currentWidth = exitStrip.getRegionWidth();
                currentHeight = exitStrip.getRegionHeight();
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
        canvas.clear();
        canvas.begin();
        canvas.drawOverlay(white, gray, true);
        canvas.draw(white, gray,0,0,12800, 720);
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
            canvas.drawFixed(currentStrip, TARGET_X, TARGET_Y);
        }else if(currentComponent != null && POLYGON_COMPONENTS.contains(currentComponent)){
            currentPolygonRegion.getVertices()[0] = -currentWidth/2f;
            currentPolygonRegion.getVertices()[2] = currentWidth/2f;
            currentPolygonRegion.getVertices()[5] = -currentHeight;
            canvas.drawFixed(currentPolygonRegion, TARGET_X, TARGET_Y);
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
            for(int j = 0; j<= heightBottom[i]; j++){
                if(tilesBottom[i] != Tile.Air){
                    canvas.draw(tilesBottom[i] == Tile.Snow? snow:water,40f*i,40f*j);
                }
            }
        }
        for(int i = 0; i<WIDTH; i++){
            for(int j = 0; j<= heightTop[i]; j++){
                if(tilesTop[i] != Tile.Air){
                    canvas.draw(snow,40f*i,40f*(18-j));
                }
            }
        }
    }

    public void drawScaledChoice(TextureRegion texture, float x, float y,float scaleX, float scaleY, float angle){
        canvas.drawFixed(texture, Color.WHITE, texture.getRegionWidth()/2,texture.getRegionHeight()/2, x,y, angle, scaleX, scaleY);
    }

    public void drawScaledChoice(PolygonRegion region , float x, float y,float scaleX, float scaleY){
        canvas.drawFixed(region, Color.WHITE, region.getRegion().getRegionWidth()/2f,  region.getRegion().getRegionWidth()/2f, x,y, 0, scaleX, scaleY);
    }

    public void drawPanel(){
        drawScaledChoice(noteLeftStrip, NOTE_X, 680, 0.5f, 0.5f, 0f);
        drawScaledChoice(iceStrip, ICE_X, 680, 0.3f, 0.5f,0f);
        drawScaledChoice(iceStrip, FLOATING_ICE_X, 680, 0.3f, 0.5f,0f);
        drawScaledChoice(iceStrip, MOVING_ICE_X, 680, 0.3f, 0.5f,0f);
        drawScaledChoice(icicleRegion, ICICLE_X, 640, 0.5f, 0.5f);
        drawScaledChoice(monsterStrip, MONSTER_X, 680, 0.5f, 0.5f,0f);
        drawScaledChoice(monsterVerStrip, MONSTER_X+80, 680, 0.5f, 0.5f,0f);
        drawScaledChoice(exitStrip, EXIT_X, 680, 0.5f, 0.5f,0f);
        canvas.drawText(displayFont, "ice", ICE_X, 680);
        canvas.drawText(displayFont, "floating", FLOATING_ICE_X-20, 680);
        canvas.drawText(displayFont, "moving", MOVING_ICE_X-20, 680);
        canvas.drawText(displayFont, "width", 1040, 660);
        canvas.drawText(displayFont, "height", 1100, 660);
        canvas.drawCircle(Color.BLACK, 1060, 680, 6);
        canvas.drawCircle(Color.BLACK, 1060, 620, 6);
        canvas.drawCircle(Color.BLACK, 1120, 680, 6);
        canvas.drawCircle(Color.BLACK, 1120, 620, 6);
        canvas.drawText(displayFont, draggingComponent == null ? "" : draggingComponent.component.name(), 1180, 700);
        canvas.drawSquare(Color.BLACK, 1160, 640, 60, 30);
        canvas.drawText(displayFont, "add", 1180, 660);
        canvas.drawSquare(Color.BLACK, 1160, 600, 60, 30);
        canvas.drawText(displayFont, "delete", 1180, 620);
        canvas.drawSquare(Color.BLACK, 1160, 560, 60, 30);
        canvas.drawText(displayFont, "finish", 1180, 580);
    }

    public void generateJson() {
        generatePlatformWater();
        generateObjects();
        writeToFile("sampleLevel.json");
        listener.updateScreen(this, GDXRoot.EDITOR_GAMEPLAY);
    }

    public void generatePlatformWater(){
        Tile prevTileBottom = Tile.Snow;
        Tile prevTileTop = Tile.Air;
        ArrayList<ArrayList<Float>> retSnow = new ArrayList<>();
        ArrayList<ArrayList<Float>> retWaterPos = new ArrayList<>();
        ArrayList<ArrayList<Float>> retWaterLayout = new ArrayList<>();
        ArrayList<Float> snowBottom = new ArrayList<>();
        ArrayList<Float> snowTop = new ArrayList<>();
        ArrayList<Float> waterPos = new ArrayList<>();
        ArrayList<Float> waterLayout = new ArrayList<>();
        snowBottom.add(0f);
        snowBottom.add(0f);
        snowBottom.add(0f);
        snowBottom.add(this.heightBottom[0]+1f);
        int waterStart = -1;
        for(int i = 0; i< tilesBottom.length; i++){
            if(tilesBottom[i] == Tile.Snow && prevTileBottom == Tile.Snow){
                if(snowBottom.get(snowBottom.size()-1) != this.heightBottom[i]+1){
                    snowBottom.add((float)i);
                    snowBottom.add(this.heightBottom[i]+1f);
                }
                snowBottom.add(i+1f);
                snowBottom.add(this.heightBottom[i]+1f);
            }else if(tilesBottom[i] == Tile.Snow && prevTileBottom == Tile.Water){
                waterPos.add((waterStart+i)/2f);
                waterPos.add((this.heightBottom[i-1]+1)/2f);
                waterLayout.add((float)i-waterStart);
                waterLayout.add(this.heightBottom[i-1]+1f);
                retWaterPos.add((ArrayList) waterPos.clone());
                retWaterLayout.add((ArrayList) waterLayout.clone());
                waterPos.clear();
                waterLayout.clear();
                snowBottom.add((float)i);
                snowBottom.add(0f);
                snowBottom.add((float)i);
                snowBottom.add(this.heightBottom[i]+1f);
                snowBottom.add(i+1f);
                snowBottom.add(this.heightBottom[i]+1f);
            }else if(tilesBottom[i] == Tile.Water && prevTileBottom == Tile.Snow){
                snowBottom.add((float)i);
                snowBottom.add(0f);
                retSnow.add((ArrayList) snowBottom.clone());
                snowBottom.clear();
                waterStart = i;
            }else if(tilesBottom[i] == Tile.Air){
                snowBottom.add((float)i);
                snowBottom.add(0f);
                retSnow.add((ArrayList) snowBottom.clone());
                break;
            }
            prevTileBottom = tilesBottom[i];
        }
        for(int i = 0; i < heightTop.length; i++){
            if(tilesTop[i] == Tile.Snow && prevTileTop == Tile.Air){
                snowTop.add((float)i);
                snowTop.add(18f);
                snowTop.add((float)i);
                snowTop.add(18f-heightTop[i]);
                snowTop.add(i+1f);
                snowTop.add(18f-heightTop[i]);
            }else if(tilesTop[i] == Tile.Snow && prevTileTop == Tile.Snow){
                snowTop.add((float)i+1);
                snowTop.add(18f-heightTop[i]);
            }else if(tilesTop[i] == Tile.Air && prevTileTop == Tile.Snow){
                snowTop.add((float)i);
                snowTop.add(18f);
                retSnow.add((ArrayList) snowTop.clone());
                snowTop.clear();
            }
            prevTileTop = tilesTop[i];
        }
        ArrayList<Float> bottom = new ArrayList<Float>(Arrays.asList(0f,-2f,0f,0f,320f,0f, 320f, -2f));
        retSnow.add(bottom);
        this.levelJson.defaults.snow = arrayListToArr(retSnow);
        this.levelJson.water.pos = arrayListToArr(retWaterPos);
        this.levelJson.water.layout = arrayListToArr(retWaterLayout);
    }

    public float[][] arrayListToArr(ArrayList<ArrayList<Float>> arr){
        float[][] ret = new float[arr.size()][];
        for (int i = 0; i < arr.size(); i++) {
            List<Float> innerList = arr.get(i);
            float[] temp = new float[innerList.size()];
            for (int k = 0; k < temp.length; k++) {
                temp[k] = innerList.get(k);
            }
            ret[i] = temp;
        }
        return ret;
    }

    public void writeToFile(String filename){
        Json json = new Json();
        String text = json.toJson(this.levelJson);
        FileHandle file = Gdx.files.local(filename);
        file.writeString(text, false);
    }

    public void generateObjects(){
        ArrayList<float[]> notePos = new ArrayList<>();
        ArrayList<float[]> icePos = new ArrayList<>();
        ArrayList<float[]> iceLayout = new ArrayList();
        ArrayList<float[]> floatingIcePos = new ArrayList<>();
        ArrayList<float[]> floatingIceLayout = new ArrayList();
        ArrayList<float[]> movingIcePos = new ArrayList<>();
        ArrayList<float[]> movingIceLayout = new ArrayList();
        ArrayList<float[]> iciclePos = new ArrayList<>();
        ArrayList<float[]> icicleLayout = new ArrayList();
        ArrayList<float[]> monsterPos = new ArrayList();
        ArrayList<Float> monsterRanges = new ArrayList();
        ArrayList<Boolean> monsterDir = new ArrayList();

        for(GenericComponent obj: objects){
            float[] pos = new float[2];
            pos[0] = obj.position.x/40f;
            pos[1] = obj.position.y/40f;
            float[] newPos;
            switch (obj.component){
                case Note:
                    newPos = new float[]{pos[0]+obj.filmStrip.getRegionWidth()/80f, pos[1]+obj.filmStrip.getRegionHeight()*7f/320f};
                    notePos.add(newPos);
                    break;
                case Icicle:
                    float[] layout = new float[obj.polygonRegion.getVertices().length];
                    for(int i = 0; i < obj.polygonRegion.getVertices().length; i++){
                        layout[i] = obj.polygonRegion.getVertices()[i] /40;
                    }
                    icicleLayout.add(layout);
                    iciclePos.add(pos);
                    break;
                case MonsterHori:
                    monsterPos.add(pos);
                    monsterRanges.add(90f);
                    monsterDir.add(true);
                    break;
                case MonsterVer:
                    monsterPos.add(pos);
                    monsterRanges.add(50f);
                    monsterDir.add(false);
                    break;
                case Ice:
                    newPos = new float[]{pos[0]+obj.filmStrip.getRegionWidth()/80f, pos[1]+obj.filmStrip.getRegionHeight()*7f/320f};
                    icePos.add(newPos);
                    iceLayout.add(new float[]{obj.filmStrip.getRegionWidth(), obj.filmStrip.getRegionHeight()});
                    break;
                case FloatingIce:
                    newPos = new float[]{pos[0]+obj.filmStrip.getRegionWidth()/80f, pos[1]+obj.filmStrip.getRegionHeight()*7f/320f};
                    floatingIcePos.add(newPos);
                    floatingIceLayout.add(new float[]{obj.filmStrip.getRegionWidth(), obj.filmStrip.getRegionHeight()});
                    break;
                case MovingIce:
                    newPos = new float[]{pos[0]+obj.filmStrip.getRegionWidth()/80f, pos[1]+obj.filmStrip.getRegionHeight()*7f/320f};
                    movingIcePos.add(newPos);
                    movingIceLayout.add(new float[]{obj.filmStrip.getRegionWidth(), obj.filmStrip.getRegionHeight()});
                    break;
                case Exit:
                    newPos = new float[]{pos[0]+obj.filmStrip.getRegionWidth()/80f, pos[1]+obj.filmStrip.getRegionHeight()/80f};
                    this.levelJson.goal.pos = newPos;
            }
        }
        this.levelJson.notes.pos = arrayListToArr2(notePos);
        this.levelJson.defaults.num_notes = notePos.size();
        this.levelJson.defaults.num_penguins = notePos.size();
        this.levelJson.ice.pos = arrayListToArr2(icePos);
        this.levelJson.ice.layout = arrayListToArr2(iceLayout);
        this.levelJson.floatingIce.pos = arrayListToArr2(floatingIcePos);
        this.levelJson.floatingIce.layout = arrayListToArr2(floatingIceLayout);
        this.levelJson.movingIce.pos = arrayListToArr2(movingIcePos);
        this.levelJson.movingIce.layout = arrayListToArr2(movingIceLayout);
        this.levelJson.icicles.pos = arrayListToArr2(iciclePos);
        this.levelJson.icicles.layout = arrayListToArr2(icicleLayout);
        this.levelJson.enemy.range = arrayListToArr1dFloat(monsterRanges);
        this.levelJson.enemy.pos = arrayListToArr2(monsterPos);
        this.levelJson.enemy.is_hor = arrayListToArr1dBoolean(monsterDir);
    }

    public float[] arrayListToArr1dFloat(ArrayList<Float> arr){
        float[] ret = new float[arr.size()];
        for(int i = 0; i<arr.size(); i++){
            ret[i] = arr.get(i);
        }
        return ret;
    }
    public boolean[] arrayListToArr1dBoolean(ArrayList<Boolean> arr){
        boolean[] ret = new boolean[arr.size()];
        for(int i = 0; i<arr.size(); i++){
            ret[i] = arr.get(i);
        }
        return ret;
    }

    public float[][] arrayListToArr2(ArrayList<float[]> arr){
        float[][] ret = new float[arr.size()][];
        for(int i = 0; i < arr.size(); i++){
            ret[i] = arr.get(i);
        }
        return ret;
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
            this.position = new Vector2(target_x , TARGET_Y);
            this.component = component;
        }
        public GenericComponent(PolygonRegion polygonRegion, Component component){
            this.polygonRegion = polygonRegion;
            this.position = new Vector2(target_x, TARGET_Y);
            this.component = component;
        }
    }

    private class LevelJson{
        public Defaults defaults = new Defaults();
        public Goal goal = new Goal();
        public Player player = new Player();
        public Penguin penguins = new Penguin();
        public Icicle icicles = new Icicle();
        public Enemy enemy = new Enemy();
        public Note notes = new Note();
        public Water water = new Water();
        public Ice ice = new Ice();
        public FloatingIce floatingIce = new FloatingIce();
        public MovingIce movingIce = new MovingIce();
    }

    private class Defaults{
        public float gravity = -9.8f;
        public float friction = 0.3f;
        public float density = 2.65f;
        public float restitution = 0.1f;
        public int num_penguins = 2;
        public int num_notes= 2;
        public float[][] snow = new float[][]{};
    }

    private class Goal{
        public float[] pos = new float[]{48f, 1.9f};
        public float friction = 0.3f;
        public float density = 2.65f;
        public float restitution = 0.1f;
        public float width = 2f;
        public float height = 2f;
    }

    private class Player{
        public float[] pos = new float[]{16f,5f};
        public float[] shrink = new float[]{0.95f, 0.7f};
        public float force = 12f;
        public float damping = 10.0f;
        public float density = 1f;
        public float friction = 0.0f;
        public float max_speed = 4f;
        public float restitution = 0f;
        public float player_jump = 7f;
        public float jump_cooldown = 30f;
        public float throw_cooldown = 30f;
        public float shoot_cooldown = 40f;
        public float punch_cool = 100f;
        public float punch_time = 30;
        public float punch_cooldown = 0f;
        public float max_throw_force = 300;
        public float sensor_height = 0.05f;
        public String sensor_name = "PlayerGroundSensor";
        public float vshrink = 0.25f;
        public float hshrink = 0.25f;
        public float sshrink = 0.6f;
        public float time_count = 0f;
    }

    private class Penguin{
        public float[] pos = new float[]{16f,5f};
        public float width = 1.3f;
        public float height = 2f;
        public float force = 20f;
        public float damping = 2f;
        public float density = 1f;
        public float friction = 0.0f;
        public float max_speed = 3f;
        public float sensor_height = 0.05f;
        public String sensor_name = "PenguinGroundSensor";
        public float vshrink = 0.75f;
        public float hshrink = 0.75f;
        public float sshrink = 0.6f;
    }

    private class  Icicle{
        public float[][] pos = new float[][]{};
        public float[][] layout = new float[][]{};
        public float density = 30f;
        public float friction = 0.5f;
        public float restitution = 0.2f;
    }

    private class Enemy{
        public float[][] pos = new float[][]{};
        public float[] range = new float[]{};
        public float width = 1.3f;
        public float height = 2f;
        public float damping = 10f;
        public float density = 1f;
        public float friction = 5f;
        public float max_speed = 5f;
        public float sensor_height = 0.05f;
        public String sensor_name = "MonsterGroundSensor";
        public float vshrink = 0.25f;
        public float hshrink = 0.25f;
        public float sshrink = 0.6f;
        public float restitution = 0.0f;
        public float force = 20f;
        public boolean[] is_hor = new boolean[]{};
    }

    private class Note{
        public float[][] pos = new float[][]{};
        public float vshrink = 0.8f;
        public float hshrink = 0.95f;
    }

    private class Water{
        public float[][] pos = new float[][]{};
        public float[][] layout = new float[][]{};
        public float density = 1f;
        public float friction = 0.5f;
        public float restitution = 0.2f;
        public float force = 20f;
    }

    private class Ice{
        public float[][] pos = new float[][]{};
        public float[][] layout = new float[][]{};
        public float friction = 2f;
        public float restitution = 0.2f;
        public float pin_radius = 0.1f;
        public float bar_density = 3f;
        public float pin_density = 0f;
    }

    private class MovingIce{
        public float[][] pos = new float[][]{};
        public float[][] layout = new float[][]{};
        public float friction = 2f;
        public float restitution = 0.2f;
        public float pin_radius = 0.1f;
        public float bar_density = 3f;
        public float pin_density = 0f;
        public float distance = 1;
        public float speed = 0.04f;
    }

    private class FloatingIce{
        public float[][] pos = new float[][]{};
        public float[][] layout = new float[][]{};
        public float friction = 2f;
        public float restitution = 0.2f;
        public float pin_radius = 0.1f;
        public float bar_density = 3f;
        public float pin_density = 0f;
    }
}
