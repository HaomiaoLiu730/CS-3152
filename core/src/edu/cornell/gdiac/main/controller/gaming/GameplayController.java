package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.GDXRoot;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.util.ScreenListener;

import java.nio.file.LinkPermission;
import java.util.ArrayList;

public class GameplayController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    private static final float MOUSE_TOL = 20f;

    private AssetDirectory internal;
    /** Reference to the character avatar */
    private Player avatar;
    /** Reference to the monster */
    private ArrayList<Monster> monsters = new ArrayList<>();
    /** Reference to the icicles */
    private ArrayList<PolygonObstacle> iciclesList;
    /** Reference to the water */
    private Water water;
    /** Reference to the ice */
    private Ice ice;
    /** Reference to the list of notes */
    private ArrayList<Note> notesList;
    /** Reference to the list of waters */
    private ArrayList<Water> waterList;
    /** Position of Reset button */
    private Vector2 resetPos;
    /** Position of Quit button */
    private Vector2 quitPos;
    /** Radius of buttons */
    private float buttonR;
    /** Whether the quit button is clicked or not */
    private boolean quitClick;
    /** Whether the reset button is clicked or not */
    private boolean resetClick;
    /** Whether the player can throw or not */
    private boolean canThrow;

    private boolean isEditingView;
    private float[] grounded;

    /** number of notes collected*/
    public static int notesCollected;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController collisionController;

    private Texture background;
    private BitmapFont gameFont ;
    private JsonValue constants;

    /** number of penguins */
    private int num_penguins;
    /** number of notes */
    private int num_notes;

    private int playerGround = 0;
    private static boolean hitWater = false;
    private boolean complete = false;
    private boolean failed = false;

    /** Cooldown (in animation frames) for punching */
    private static  int PUNCH_COOLDOWN;
    /** Length (in animation frames) for punching */
    private static  int PUNCH_TIME;
    private int punchCooldown;
    /** resetCountdown */
    public static int resetCountDown = 30;

    private String jsonFile;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS ;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;
    /** camera absolute position*/
    private float cameraX;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    private int currentLevelNum;

    ArrayList<Obstacle> staticBodies = new ArrayList<>();

    /**
     * Creates a new game with a playing field of the given size.
     * <p>
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     *
     * @param width  The width of the game window
     * @param height The height of the game window
     */
    public GameplayController(float width, float height, boolean isEditingView, String jsonFile, int level) {
        super(width,height,DEFAULT_GRAVITY);

        currentLevelNum = level;
        scale = super.scale;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);

        this.jsonFile = jsonFile;

        internal = new AssetDirectory(isEditingView ? "levelEditor.json" :jsonFile);
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);

        collisionController = new CollisionController(width, height);
        sensorFixtures = new ObjectSet<Fixture>();

        constants = internal.getEntry( "level"+(level+1), JsonValue.class );
        JsonValue defaults = constants.get("defaults");
        num_penguins = defaults.getInt("num_penguins",0);
        num_notes = defaults.getInt("num_notes",0);
        this.isEditingView = isEditingView;
        grounded = defaults.get("grounded").asFloatArray();
    }

    public void setJsonValue(JsonValue jsonValue){

    }

    public GameplayController(String jsonFile, int level){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, false, jsonFile, level);
    }

    public GameplayController(boolean isEditorView, String jsonFile, int level){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, isEditorView, jsonFile, level);
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    public void setComplete(boolean val){
        this.complete = val;
    }

    public void setFailure(boolean val){
        this.failed = val;
    }

    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param directory Reference to global asset manager.
     */
    public void loadContent(AssetDirectory directory) {
        super.loadContent(directory);
        if (platformAssetState != AssetState.LOADING) {
            return;
        }
        platformAssetState = AssetState.COMPLETE;
    }


    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        notesCollected = 0;
        hitWater(false);
        Vector2 gravity = new Vector2(world.getGravity());

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        monsters.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
        resetCountDown = 50;
        quitClick = false;
        resetClick = false;
        canThrow = false;

        canvas.getCamera().viewportWidth = 1280;
        canvas.getCamera().viewportHeight = 720;
        canvas.getCamera().position.x = 1280/2;
        canvas.getCamera().position.y = 720/2;
        cameraX = canvas.getCamera().position.x;
        if(avatar.getX()/32*1280 > cameraX){
            canvas.getCamera().translate(avatar.getX()/32*1280-cameraX, 0f);
            canvas.getCamera().update();
            cameraX = canvas.getCamera().position.x;
        }
        else if(avatar.getX()/32*1280 < cameraX){
            canvas.getCamera().translate(avatar.getX()/32*1280-cameraX, 0f);
            canvas.getCamera().update();
            cameraX = canvas.getCamera().position.x;
        }
        canvas.getCamera().update();
        collisionController = new CollisionController(1280, 720);

        sensorFixtures = new ObjectSet<Fixture>();
        internal = new AssetDirectory(isEditingView ? "levelEditor.json" : this.jsonFile);
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);


        constants = internal.getEntry( "level"+(this.currentLevelNum+1), JsonValue.class );
        JsonValue defaults = constants.get("defaults");
        num_penguins = defaults.getInt("num_penguins",0);
        num_notes = defaults.getInt("num_notes",0);

    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        quitPos = new Vector2(canvas.getWidth()-60f, canvas.getHeight()-30.0f);
        resetPos = new Vector2(canvas.getWidth()-120f, canvas.getHeight()-30.0f);
        buttonR = 20;

        // Add level goal
        float dwidth, dheight;
        JsonValue defaults = constants.get("defaults");
        String sname = "snow";
        for (int ii = 0; ii < defaults.get("snow").size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(defaults.get("snow").get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0));
            obj.setFriction(defaults.getFloat("friction", 0));
            obj.setRestitution(defaults.getFloat("restitution", 0));
            obj.setDrawScale(scale);
            obj.setTexture(snowTextureRegion);
            obj.setName(sname+ii);
            addObject(obj);
        }

        JsonValue icicles = constants.get("icicles");
        JsonValue iciclepos = icicles.get("pos");

        iciclesList = new ArrayList<PolygonObstacle>();
        for (int i = 0; i < icicles.get("pos").size; i ++){
            PolygonObstacle icicle;
            icicle = new PolygonObstacle(icicles.get("layout").get(i).asFloatArray(), iciclepos.get(i).getFloat(0), iciclepos.get(i).getFloat(1));            
            icicle.setBodyType(BodyDef.BodyType.StaticBody);
            icicle.setDensity(icicles.getFloat("density"));
            icicle.setFriction(icicles.getFloat("friction"));
            icicle.setRestitution(icicles.getFloat("restitution"));
            icicle.setDrawScale(scale);
            icicle.setTexture(icicleStrip);
            icicle.setName("icicle" + i);
            addObject(icicle);
            iciclesList.add(icicle);
        }

        JsonValue goal = constants.get("goal");
        JsonValue goalpos=goal.get("pos");
        BoxObstacle exit;
        exit = new BoxObstacle( goalpos.getFloat(0), goalpos.getFloat(1), goal.getFloat(("width")), goal.getFloat(("height")));
        exit.setBodyType(BodyDef.BodyType.StaticBody);
        exit.setSensor(true);
        exit.setDensity(goal.getFloat("density"));
        exit.setFriction(goal.getFloat("friction"));
        exit.setRestitution(goal.getFloat("restitution"));
        exit.setDrawScale(scale);
        exit.setName("exit");
        exit.setTexture(exitStrip);
        addObject(exit);

        // Create player
        dwidth  = avatarStrip.getRegionWidth()/scale.x;
        dheight = avatarStrip.getRegionHeight()/scale.y;
        PUNCH_COOLDOWN=constants.get("player").getInt("punch_cool");
        PUNCH_TIME=constants.get("player").getInt("punch_time");
        punchCooldown=constants.get("player").getInt("punch_cooldown");
        avatar = new Player(constants.get("player"),constants.get("penguins"), dwidth, dheight, num_penguins);
        avatar.setDrawScale(scale);
        avatar.setFilmStrip(avatarStrip);
        avatar.setArrowTexture(arrowTexture);
        avatar.setEnergyBar(energyBarTexture);
        avatar.setEnergyBarOutline(energyBarOutlineTexture);
        avatar.setJumpHangingStrip(jumpHangingStrip);
        avatar.setJumpLandingStrip(jumpLandingStrip);
        avatar.setJumpRisingStrip(jumpRisingStrip);
        avatar.setWalkingStrip(avatarStrip);
        avatar.setThrowingStrip(throwingStrip);
        avatar.setPenguinWalkingStrip((penguinWalkingStrip));
        avatar.setPenguinRollingStrip(penguinRollingStrip);
        addObject(avatar);

        for(int i = 0; i<num_penguins; i++){
            avatar.getPenguins().get(i).setDrawScale(scale);
            avatar.getPenguins().get(i).setWalkingStrip(penguinWalkingStrip);
            avatar.getPenguins().get(i).setRolllingFilmStrip(penguinRollingStrip);
            addObject(avatar.getPenguins().get(i));
            avatar.getPenguins().get(i).getBody().setType(BodyDef.BodyType.DynamicBody);
            avatar.getPenguins().get(i).setFilmStrip(penguinWalkingStrip);
        }

        JsonValue enemy = constants.get("enemy");
        JsonValue enemyPos = enemy.get("pos");
        JsonValue enemyRange = enemy.get("range");
        JsonValue enemyDir = enemy.get("is_hor");
        for (int i=0; i < enemyPos.size; i++) { //multiple monsters
            Monster monster = new Monster(enemy, enemyPos.get(i).getFloat(0), enemyPos.get(i).getFloat(1),
                    monsterStrip.getRegionWidth() / scale.x, monsterStrip.getRegionHeight() / scale.y,
                    "monster", enemyRange.getInt(i), enemyDir.getBoolean(i));
            monster.setFilmStrip(monsterStrip);
            monster.setDrawScale(scale);
            monsters.add(monster);
            addObject(monster);
        }

        JsonValue notes = constants.get("notes");
        JsonValue notespos = notes.get("pos");
        notesList = new ArrayList<Note>();
        for (int i =0; i< notespos.size; i++) {
            Note note = new Note(notes, noteLeftStrip.getRegionWidth() / scale.x, noteLeftStrip.getRegionHeight() / scale.y, i );
            note.setFilmStrip(noteLeftStrip);
            note.setDrawScale(scale);
            addObject(note);
            notesList.add(note);
        }

        JsonValue ices = constants.get("ice");
        JsonValue icepos = ices.get("pos");
        for (int i =0; i< icepos.size; i++) {
            int w = ices.get("layout").get(i).getInt(0);
            int h = ices.get("layout").get(i).getInt(1);
            ice = new Ice(ices, i, w/scale.x, h/scale.y);
            iceTextureRegion.setRegionWidth(w);
            iceTextureRegion.setRegionHeight(h);
            ice.setDrawScale(scale);
            ice.setTexture(iceTextureRegion);
            ice.setRestitution(ices.getFloat("restitution"));
            addObject(ice);
        }

        JsonValue fices = constants.get("floatingIce");
        JsonValue ficepos = fices.get("pos");
        FloatingIce fIce;
        for (int i =0; i< ficepos.size; i++) {
        int w = fices.get("layout").get(i).getInt(0);
        int h = fices.get("layout").get(i).getInt(1);
            fIce = new FloatingIce(fices, i, w/scale.x, h/scale.y);
            ficeTextureRegion.setRegionWidth(w);
            ficeTextureRegion.setRegionHeight(h);
            fIce.setDrawScale(scale);
            fIce.setTexture(ficeTextureRegion);
            fIce.setRestitution(fices.getFloat("restitution"));
            addObject(fIce);
        }


        JsonValue mices = constants.get("movingIce");
        JsonValue micepos = mices.get("pos");
        MovingIce mIce;
        for (int i =0; i< micepos.size; i++) {
            int w = mices.get("layout").get(i).getInt(0);
            int h = mices.get("layout").get(i).getInt(1);
            mIce = new MovingIce(mices, i, w/scale.x, h/scale.y);
            miceTextureRegion.setRegionWidth(w);
            miceTextureRegion.setRegionHeight(h);
            mIce.setDrawScale(scale);
            mIce.setTexture(miceTextureRegion);
            mIce.setRestitution(mices.getFloat("restitution"));
            addObject(mIce);
        }
        JsonValue waters = constants.get("water");
        JsonValue water_layout = waters.get("layout");
        waterList= new ArrayList<Water>();
        for (int i =0; i< water_layout.size; i++) {
            water = new Water(waters, water_layout.get(i).getFloat(0),water_layout.get(i).getFloat(1), "water",i);
            water.setFilmStrip(waterStrip, wavesStrip);
            water.setDrawScale(scale);
            waterList.add(water);
            addObject(water);
            water.setActive(false);
            water.setAwake(false);
        }

    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        if (!failed && avatar.getY() < -1) {
            setFailure(true);
            return false;
        }

        return true;
    }

    @Override
    public void dispose() {
        internal.dispose();
        internal = null;
        collisionController = null;
        canvas = null;
    }

    @Override
    public void update(float dt) {
        for(Obstacle obj: staticBodies){
            obj.setBodyType(BodyDef.BodyType.StaticBody);
        }
        staticBodies.clear();
        if (Math.abs(Gdx.input.getX() - resetPos.x) <= MOUSE_TOL && Math.abs(720 - Gdx.input.getY() - resetPos.y) <= MOUSE_TOL) {
            if (Gdx.input.isTouched()) {
                hitWater(true);
                resetClick = true;
                return;
            }
        }
        if (InputController.getInstance().touchUp() && Math.abs(Gdx.input.getX() - quitPos.x) <= MOUSE_TOL && Math.abs(720 - Gdx.input.getY() - quitPos.y) <= MOUSE_TOL) {
            listener.updateScreen(this, GDXRoot.GAMEPLAY_MENU);
            quitClick = true;
            return;
        }
        if(resetCountDown < 0 && !failed){
            if(!isEditingView){
                this.listener.updateScreen(this, currentLevelNum);
                return;
            }else{
                reset();
            }
        }

        backToEdit();
        updateCamera();
        updatePlayer();

        if(complete){
            resetCountDown-=1;
        }
        if(resetCountDown < 0 && failed){
            reset();
        }

        // debug mode
        if(InputController.getInstance().didDebug()){
            setDebug(!isDebug());
        }

        // Punching
        if (InputController.getInstance().didPunch() && punchCooldown <= 0) {
            avatar.setFilmStrip(punchStrip);
            avatar.setPunching(true);
            punchCooldown = PUNCH_COOLDOWN;
        } else {
            punchCooldown -= 1;
        }
        if (punchCooldown == PUNCH_COOLDOWN - PUNCH_TIME) {
            avatar.setFilmStrip(avatarStrip);
            avatar.setPunching(false);
        }

        // Losing condition
        if(hitWater){
            setFailure(true);
            setComplete(true);
        }

        // Monster moving and attacking
        collisionController.processCollision(monsters, avatar, objects);
        if(collisionController.processCollision(monsters, attackStrip, avatar.getPenguins())){
            setFailure(true);
            setComplete(true);
        }
        collisionController.processCollision(monsters, iciclesList, objects);
        collisionController.processCollision(avatar.getPenguins(), iciclesList, objects);
        collisionController.processCollision(waterList, avatar);

        notesCollected = collisionController.penguin_note_interaction(avatar.getPenguins(), notesList, noteCollectedStrip, notesCollected,
                objects, avatar.getNumPenguins(), avatar);

    }

    public void backToEdit(){
        if(isEditingView && (InputController.getInstance().getClickX() > 1200 &&
                InputController.getInstance().getClickX() < 1260 &&
                InputController.getInstance().getClickY() < 160 &&
                InputController.getInstance().getClickY() > 120) &&
                InputController.getInstance().touchUp()){
            this.listener.updateScreen(this, GDXRoot.GAMEPLAY_EDITOR);
        }
    }


    public void updateCamera(){
        // camera
        if(avatar.getX()>16){
            if(avatar.getX()/32*1280 > cameraX){
                canvas.getCamera().translate(avatar.getX()/32*1280-cameraX, 0f);
                canvas.getCamera().update();
                cameraX = canvas.getCamera().position.x;
            }
            else if(avatar.getX()/32*1280 < cameraX){
                canvas.getCamera().translate(avatar.getX()/32*1280-cameraX, 0f);
                canvas.getCamera().update();
                cameraX = canvas.getCamera().position.x;
            }
        }
        avatar.setCameraX(cameraX);
    }

    public void updatePlayer(){
        // avatar motion
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.applyForce();
        if(avatar.isJumping()&&InputController.getInstance().didPrimary()){
            avatar.moveState = Player.animationState.jumpRising;
            avatar.setFilmStrip(jumpRisingStrip);
        }
        avatar.setThrowing(InputController.getInstance().getClickX(),
                InputController.getInstance().getClickY(),
                InputController.getInstance().touchUp() && !resetClick && canThrow,
                InputController.getInstance().isTouching());
        canThrow = true;
        avatar.pickUpPenguins();
    }

    /**
     * Draw the physics objects together with foreground and background
     *
     * This is completely overridden to support custom background and foreground art.
     *
     * @param dt Timing values from parent loop
     */
    public void draw(float dt) {
        if (quitClick) return;
        // TODO: fix this
        if(canvas==null){
            return;
        }
        canvas.clear();

        canvas.begin();
        canvas.drawBackground(background,0, -100);

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        String noteMsg = "Notes collected: "+ notesCollected + "/"+num_notes;
        String penguinMsg = "Penguins: "+ avatar.getNumPenguins() + "/"+num_penguins;
        canvas.drawText( gameFont, noteMsg,5.0f, canvas.getHeight()-5.0f);
        canvas.drawText( gameFont, penguinMsg,5.0f, canvas.getHeight()-40.0f);
        if(isEditingView){
            canvas.drawSquare(Color.BLACK,1200,560,60,40);
            canvas.drawText(gameFont, "Edit", 1200,600);
        }else{
            canvas.drawCircle(Color.FIREBRICK, quitPos.x, quitPos.y, buttonR);
            canvas.drawText( gameFont, "Quit", quitPos.x-15f, quitPos.y-30f);
            canvas.drawCircle(Color.TEAL, resetPos.x, resetPos.y, buttonR);
            canvas.drawText( gameFont, "Reset",resetPos.x-25f, resetPos.y-30f);
        }
        canvas.end();

        if (isDebug()) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        // Final message
        if (complete && !failed) {
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("VICTORY!", gameFont, 0.0f);
            canvas.end();
        } else if (failed) {
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("FAILURE!", gameFont, 0.0f);
            canvas.end();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    public static void hitWater(boolean value){
        hitWater = value;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // See if we have landed on the ground.
            boolean bd1IsGround = !(bd1 instanceof Penguin) && !(bd1 instanceof Note) && (!(bd1 instanceof Water));
            boolean bd2IsGround = !(bd2 instanceof Penguin) && !(bd2 instanceof Note) && (!(bd2 instanceof Water));
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1 && bd1IsGround) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2 && bd2IsGround)) {
                avatar.setGrounded(true);
                playerGround += 1;
                if(avatar.moveState == Player.animationState.jumpHanging){
                    avatar.moveState = Player.animationState.jumpLanding;
                    avatar.setFilmStrip(jumpLandingStrip);
                }
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            if(bd1.getName().startsWith("snow") && bd2.getName().startsWith("dude") ||
                    bd2.getName().startsWith("snow") && bd1.getName().startsWith("dude")){
                avatar.setGrounded(true);
            }

            if(bd1.getName().startsWith("icicle") && bd1.getBodyType() == BodyDef.BodyType.StaticBody && bd2.getName().startsWith("dude") ||
                    bd2.getName().startsWith("icicle") && bd2.getBodyType() == BodyDef.BodyType.StaticBody && bd1.getName().startsWith("dude")){
                avatar.setGrounded(true);
            }

            // check whether the penguin is grounded
            for(Penguin p: avatar.getPenguins()){
                if ((p.getSensorName().equals(fd2) && p != bd1 && bd1 != avatar) ||
                        (p.getSensorName().equals(fd1) && p != bd2 && bd2 != avatar)) {
                    p.setGrounded(true);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if(bd1.getName().startsWith("snow") && bd2.getName().startsWith("icicle")){
                int index = Integer.parseInt(bd1.getName().substring(bd1.getName().length()-1));
                for(float i:grounded){
                    if(index == i){
                        staticBodies.add(bd2);
                    }
                }
            }
            if(bd2.getName().startsWith("snow") && bd1.getName().startsWith("icicle")){
                int index = Integer.parseInt(bd2.getName().substring(bd2.getName().length()-1));
                for(float i:grounded){
                    if(index == i){
                        staticBodies.add(bd1);
                    }
                }
            }

            // set the ice bar tilt only for avatar
            if ((bd1.getName()=="iceBar" || bd1.getName()=="floatingIceBar") && bd2 == avatar) {
                bd1.setFixedRotation(false);
            }
            if (bd1 == avatar && (bd2.getName()=="iceBar"|| bd2.getName()=="floatingIceBar")) {
                bd2.setFixedRotation(false);
            }

            // Check for win condition
            if ((bd1.getName() == "exit" && bd2 == avatar && notesCollected == num_notes) ||
                    (bd1 == avatar && bd2.getName() == "exit" && notesCollected == num_notes)) {
                setComplete(true);
            }

            //contact for floating ice bar
            if(bd1.getName() == "floatingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
                if(bd2.getName().startsWith("icicle") ){
                    float force = bd2.getMass()/2000;
                    if (bd2.getX()<bd1.getX()){
                        force = -force;
                    }
                    ((FloatingIce)master).hitByIcicle(force);
                }
                else if (!(bd1 instanceof Penguin)){
                    ((FloatingIce)master).resetMomentum();
                }

            }

            if(bd2.getName() == "floatingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd2).getMaster();
                if(bd1.getName().startsWith("icicle")){
                    float force = bd1.getMass()/2000;
                    if (bd1.getX()<bd2.getX()){
                        force = -force;
                    }
                    ((FloatingIce)master).hitByIcicle(force);
                }
                else if (!(bd1 instanceof Penguin)){
                    ((FloatingIce)master).resetMomentum();
                }
            }

            //contact for moving ice bar
            if(bd1.getName() == "movingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
                if(bd2.getName().startsWith("monster") ){
                    Monster m = (Monster) bd2;
                    ((MovingIce) master).addMonster(m);
                }
                else if (bd2 instanceof Player){
                    Player p = (Player) bd2;
                    ((MovingIce) master).addPlayer(p);
                }

            }

            if(bd2.getName() == "movingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd2).getMaster();
                if(bd1.getName().startsWith("monster") ){
                    Monster m = (Monster) bd1;
                    ((MovingIce) master).addMonster(m);
                }
                else if (bd1 instanceof Player){
                    Player p = (Player) bd1;
                    ((MovingIce) master).addPlayer(p);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd11 = body1.getUserData();
        Object bd22 = body2.getUserData();

        boolean bd1IsGround = !(bd11 instanceof Penguin) && !(bd11 instanceof Note) && (!(bd11 instanceof Water));
        boolean bd2IsGround = !(bd22 instanceof Penguin) && !(bd22 instanceof Note) && (!(bd22 instanceof Water));
        if ((avatar.getSensorName().equals(fd2) && avatar != bd11 && bd1IsGround) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd22 && bd2IsGround)) {
            sensorFixtures.remove(avatar == bd11 ? fix2 : fix1);
            playerGround -= 1;
            if (playerGround == 0) {
                avatar.setGrounded(false);
            }
        }

        for(Penguin p: avatar.getPenguins()){
            if ((p.getSensorName().equals(fd2) && p != bd11 && bd11 != avatar) ||
                    (p.getSensorName().equals(fd1) && p != bd22 && bd22 != avatar)) {
                sensorFixtures.remove(p == bd11 ? fix2 : fix1);
                p.setGrounded(false);
            }
        }

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

        if(bd1.getName() == "movingIceBar"){
            ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
            if(bd2.getName().startsWith("monster") ){
                Monster m = (Monster) bd2;
                ((MovingIce) master).removeMonster(m);
            }
            else if (bd2 instanceof Player){
                ((MovingIce) master).removePlyaer();
            }

        }

        if(bd2.getName() == "movingIceBar"){
            ComplexObstacle master = ((BoxObstacle)bd2).getMaster();
            if(bd1.getName().startsWith("monster") ){
                Monster m = (Monster) bd1;
                ((MovingIce) master).removeMonster(m);
            }
            else if (bd1 instanceof Player){
                ((MovingIce) master).removePlyaer();
            }

        }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

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