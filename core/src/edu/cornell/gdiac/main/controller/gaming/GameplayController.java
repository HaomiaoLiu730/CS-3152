package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.GDXRoot;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;


import javax.swing.*;
import java.nio.file.LinkPermission;
import java.util.ArrayList;

import static edu.cornell.gdiac.main.GDXRoot.GAMEPLAY_CONTINUE;
import static edu.cornell.gdiac.main.GDXRoot.GAMEPLAY_MENU;

public class GameplayController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    private static final float MOUSE_TOL = 50f;

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
    private boolean closeAttemp;

    private boolean isEditingView;
    private float[] grounded;

    private boolean isPaused;
    private boolean disableMovement;
    private ArrayList<Boolean> pauseList;
    private ArrayList<Boolean> tiltList;

    private LevelLoader levelLoader;

    private int timeCounter;

    private AssetLoader assetLoader = GDXRoot.assetLoader;


    /** number of notes collected*/
    public static int notesCollected;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController collisionController;

    private Texture background;
    private BitmapFont gameFont ;
    private boolean endSoundPlaying;

    /** number of penguins */
    private int num_penguins;
    /** number of notes */
    private int num_notes;

    private int playerGround = 0;
    private static boolean hitWater = false;
    private boolean complete = false;
    private boolean failed = false;
    private Vector2 forceCache = new Vector2();

    /** Cooldown (in animation frames) for punching */
    private static  int PUNCH_COOLDOWN;
    /** Length (in animation frames) for punching */
    private static  int PUNCH_TIME;
    private int punchCooldown;
    private int level;
    /** resetCountdown */
    public static final int RESETCD = 100;
    public static int resetCountDown = RESETCD;

    private String jsonFile;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;
    /** camera absolute position*/
    private float cameraX;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    private int currentLevelNum;

    ArrayList<Integer> staticBodies = new ArrayList<>();
    ArrayList<Boolean> icicles_hit = new ArrayList<>();


    /**
     * Creates a new game with a playing field of the given size.
     * <p>
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     *
     * @param width  The width of the game window
     * @param height The height of the game window
     */
    public GameplayController(float width, float height, boolean isEditingView) {
        super(width,height,DEFAULT_GRAVITY*2.5f);
        currentLevelNum = level;
        scale = super.scale;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);

        this.jsonFile = jsonFile;
        this.level = level;
        endSoundPlaying = false;

        collisionController = new CollisionController(width, height);
        sensorFixtures = new ObjectSet<Fixture>();

        this.isEditingView = isEditingView;

    }

    public void setJsonValue(JsonValue jsonValue){

    }

    public GameplayController(){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
    }

    public GameplayController(boolean isEditorView){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, isEditorView);
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

    public void loadLevel(LevelLoader levelLoader){
        this.levelLoader = levelLoader;
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
        num_penguins = levelLoader.num_penguins;
        num_notes = levelLoader.num_notes;
        grounded = levelLoader.grounded;
        disableMovement = false;
        endSoundPlaying = false;
        notesCollected = 0;
        hitWater(false);
        Vector2 gravity = new Vector2(world.getGravity());

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
        resetCountDown = RESETCD;
        quitClick = false;
        resetClick = false;
        canThrow = false;
        staticBodies.clear();
        icicles_hit.clear();
        for (PolygonObstacle icicle: levelLoader.iciclesList) {
            staticBodies.add(0);
            icicles_hit.add(false);
        }

        canvas.getCamera().viewportWidth = 1280;
        canvas.getCamera().viewportHeight = 720;
        canvas.getCamera().position.x = 1280/2;
        canvas.getCamera().position.y = 720/2;
        cameraX = canvas.getCamera().position.x;
        if(levelLoader.avatar.getX()/32*1280 > cameraX){
            canvas.getCamera().translate(levelLoader.avatar.getX()/32*1280-cameraX, 0f);
            canvas.getCamera().update();
            cameraX = canvas.getCamera().position.x;
        }
        else if(levelLoader.avatar.getX()/32*1280 < cameraX){
            canvas.getCamera().translate(levelLoader.avatar.getX()/32*1280-cameraX, 0f);
            canvas.getCamera().update();
            cameraX = canvas.getCamera().position.x;
        }
        canvas.getCamera().update();
        collisionController = new CollisionController(1280, 720);

        sensorFixtures = new ObjectSet<Fixture>();
        background = levelLoader.background;
        gameFont = levelLoader.gameFont;

    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        quitPos = new Vector2(canvas.getWidth()-80f, canvas.getHeight()-80f);
        buttonR = 20;
        levelLoader = levelLoader.reset();

        for(PolygonObstacle snow: levelLoader.snowList){
            addObject(snow);
        }
        for(PolygonObstacle icicle: levelLoader.iciclesList){
            addObject(icicle);
        }

        addObject(levelLoader.exit);
        addObject(levelLoader.avatar);
        for(Penguin p: levelLoader.avatar.getPenguins()){
            addObject(p);
            p.getBody().setType(BodyDef.BodyType.DynamicBody);
            p.setActive(false);
            p.setSensor(true);
        }
        addObject(levelLoader.avatar.pseudoPenguin);
        levelLoader.avatar.pseudoPenguin.getBody().setType(BodyDef.BodyType.DynamicBody);
        levelLoader.avatar.pseudoPenguin.setActive(false);
        levelLoader.avatar.pseudoPenguin.setSensor(true);

        for(Note note: levelLoader.notesList){
            addObject(note);
        }
        for(Water water: levelLoader.waterList){
            addObject(water);
            // water.setBodyType(BodyDef.BodyType.StaticBody);
            // water.setSensor(true);
            water.setActive(false);
            water.setAwake(false);
        }

        for(Ice ice: levelLoader.iceList){
            addObject(ice);
        }
        for(FloatingIce floatingIce: levelLoader.floatingIcesList){
            addObject(floatingIce);
        }

        for(MovingIce movingIce: levelLoader.movingIcesList){
            addObject(movingIce);
        }

        for(int i=0;i<7;i++){
            assetLoader.BackgroundMusic[i].stop();
        }
        for(int i=0;i<levelLoader.num_penguins;i++){
            if(i>6) break;
            assetLoader.BackgroundMusic[i].loop();
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
     *
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        if (!failed && levelLoader.avatar.getY() < -1) {
            setFailure(true);
            return false;
        }

        return true;
    }

    @Override
    public void dispose() {
//        internal.dispose();
//        internal = null;
        collisionController = null;
        canvas = null;
    }

    @Override
    public void update(float dt) {
        if(isPaused){
            if(InputController.getInstance().touchUp() &&( Gdx.input.getX()< 450 ||Gdx.input.getX()> 840
                    ||Gdx.input.getY()<140 || Gdx.input.getY() > 510)){

                isPaused = false;
                disableMovement = false;
//                for (int i=0; i<objects.size(); i++){
//                    if (pauseList.get(i)) {
//                        objects.get(i).setActive(true);
//                    }
//                    if (tiltList.get(i)) {
//                        if (objects.get(i).getName() == "floatingIce") {
//                            FloatingIce fice = (FloatingIce) objects.get(i);
//                            fice.getIceBar().setAngularDamping(0.5f);
//                            fice.getIceBar().setFixedRotation(false);
//                        } else {
//                            Ice ice = (Ice) objects.get(i);
//                            ice.getIceBar().setAngularDamping(0.5f);
//                            ice.getIceBar().setFixedRotation(false);
//                        }
//                    }
//                    objects.get(i).setPaused(false);
//                }
            }
            return;
        }

        if (InputController.getInstance().touchUp() && Math.abs(Gdx.input.getX() - quitPos.x) <= MOUSE_TOL && Math.abs(720 - Gdx.input.getY() - quitPos.y) <= MOUSE_TOL) {
            isPaused = true;
            levelLoader.avatar.setThrowing(InputController.getInstance().touchUp(), throwingP,true);
            disableMovement = true;
//            pauseList = new ArrayList<>();
//            tiltList = new ArrayList<>();
//            for (int i=0; i<objects.size(); i++){
//                if (objects.get(i).isActive()) {
//                    pauseList.add(true);
//                    objects.get(i).setActive(false);
//                } else {
//                    pauseList.add(false);
//                }
//                if (objects.get(i).getName() == "floatingIce") {
//                    FloatingIce fice = (FloatingIce) objects.get(i);
//                    if (!fice.getIceBar().isFixedRotation()) {
//                        tiltList.add(true);
//                        fice.getIceBar().setAngularDamping(0);
//                        fice.getIceBar().setAngularVelocity(0);
//                        fice.getIceBar().setFixedRotation(true);
//                    } else {
//                        tiltList.add(false);
//                    }
//                } else if (objects.get(i).getName() == "Ice") {
//                    Ice ice = (Ice) objects.get(i);
//                    if (!ice.getIceBar().isFixedRotation()) {
//                        tiltList.add(true);
//                        ice.getIceBar().setAngularDamping(0);
//                        ice.getIceBar().setAngularVelocity(0);
//                        ice.getIceBar().setFixedRotation(true);
//                    } else {
//                        tiltList.add(false);
//                    }
//                } else {
//                    tiltList.add(false);
//                }
//                objects.get(i).setPaused(true);
//            }
            return;
        }

        for (int i = 0; i < levelLoader.iciclesList.size(); i++) {
            if (staticBodies.get(i) == 1) {
                levelLoader.iciclesList.get(i).setBodyType(BodyDef.BodyType.StaticBody);
                staticBodies.set(i, 2);
            }
        }

        if (resetCountDown < 0 && !failed) {
            if (!isEditingView) {
                this.listener.updateScreen(this, currentLevelNum);
                return;
            } else {
                reset();
            }
        }

        backToEdit();
        updateCamera();
        if(!disableMovement){
            updatePlayer();
        }

        if (complete) {
            resetCountDown -= 1;
        }
        if ((resetCountDown < 0 && failed) ) {
            reset();
        }

        // debug mode
        if (InputController.getInstance().didDebug()) {
            setDebug(!isDebug());
        }

        // Punching
        if (InputController.getInstance().didPunch() && punchCooldown <= 0) {
            levelLoader.avatar.setFilmStrip(assetLoader.punchStrip);
            levelLoader.avatar.setPunching(true);
            assetLoader.punching.play();
            punchCooldown = PUNCH_COOLDOWN;
        } else {
            punchCooldown -= 1;
        }
        if (punchCooldown == PUNCH_COOLDOWN - PUNCH_TIME) {
            // TODO:
            levelLoader.avatar.setFilmStrip(assetLoader.avatarStrip);
            levelLoader.avatar.setPunching(false);
        }

        // Losing condition
        if (hitWater) {
            setFailure(true);
            setComplete(true);
        }

        // Monster moving and attacking
//        collisionController.processCollision(seals, sealions, levelLoader.avatar, objects);
//        if (collisionController.processCollision(seals, sealions, attackStrip, levelLoader.avatar.getPenguins())) {
//            setFailure(true);
//            setComplete(true);
//        }
//        collisionController.processCollision(seals, sealions, iciclesList, objects);
        collisionController.processCollision(levelLoader.iciclesList, icicles_hit, staticBodies, objects,assetLoader.hitIcicle);
        collisionController.processCollision(levelLoader.waterList, levelLoader.avatar);
        collisionController.processCollision(levelLoader.waterList, levelLoader.avatar.getPenguins(),levelLoader.avatar);

        notesCollected = collisionController.penguin_note_interaction(levelLoader.avatar.getPenguins(), levelLoader.notesList, assetLoader.noteCollectedStrip, notesCollected,
                objects, levelLoader.avatar.getNumPenguins(), levelLoader.avatar, assetLoader.collectingNote, assetLoader.penguins);

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
        // leave this for ending  levelLoader.avatar.getX() < constants.get("goal").get("pos").getFloat(0)
        float maxX = levelLoader.exit.getX() < 16 ? 320 : levelLoader.exit.getX();
        if(levelLoader.avatar.getX()>16 && levelLoader.avatar.getX() < maxX){
            if(levelLoader.avatar.getX()/32*1280 > cameraX){
                canvas.getCamera().translate(levelLoader.avatar.getX()/32*1280-cameraX, 0f);
                canvas.getCamera().update();
                cameraX = canvas.getCamera().position.x;
            }
            else if(levelLoader.avatar.getX()/32*1280 < cameraX){
                canvas.getCamera().translate(levelLoader.avatar.getX()/32*1280-cameraX, 0f);
                canvas.getCamera().update();
                cameraX = canvas.getCamera().position.x;
            }
        }
        levelLoader.avatar.setCameraX(cameraX);
    }

    public void updatePlayer(){
        // levelLoader.avatar motion
        levelLoader.avatar.pickUpPenguins();
        levelLoader.avatar.setMovement(InputController.getInstance().getHorizontal() * levelLoader.avatar.getForce());
        levelLoader.avatar.setJumping(InputController.getInstance().didPrimary());
        levelLoader.avatar.applyForce();
        if(levelLoader.avatar.isJumping()&&InputController.getInstance().didPrimary()){
            levelLoader.avatar.moveState = Player.animationState.jumpRising;
            levelLoader.avatar.setFilmStrip(assetLoader.jumpRisingStrip);
            assetLoader.jumping.play();
        }
        levelLoader.avatar.setThrowing(InputController.getInstance().touchUp(), assetLoader.throwingP,Gdx.input.isKeyPressed(Input.Keys.SPACE));
        canThrow = true;
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

        if(canvas==null){
            return;
        }
        canvas.clear();

        canvas.begin();
        canvas.drawBackground(background,0, 0);
        // draw tutorial text;
        if(levelLoader.jsonFile.startsWith("europe")){
            switch (levelLoader.getLevel()){
                case 0:
                    canvas.drawText("Use 'WASD' or arrow keys \nto move or jump.", gameFont,500, 500);
                    canvas.drawText("Ice bars will tilt.", gameFont,1280, 320);
                    canvas.drawText("Approach a note to collect it, \nyou will lose a penguin for each note you collect. ",gameFont,1800, 500);
                    canvas.drawText("Collect all the notes to pass the level! ", gameFont, 1950, 300);
                    break;
                case 1:
                    canvas.drawText("Some ice bars move!", gameFont,1500, 400);
                    break;
                case 2:
                    assetLoader.teachThrowingStrip.nextFrame();
                    canvas.drawText("Throw the penguin at the icicle to knock it down!", gameFont,640, 640);
                    canvas.drawText("You can cancel throwing by pressing the space bar", gameFont,640, 600);
                    canvas.draw(assetLoader.teachThrowingStrip, 900, 400);
                    canvas.drawText("Nearby penguins will be recollected", gameFont,860, 360);
                    canvas.drawText("Throw the a penguin \nat the note to collect it!", gameFont,2000, 600);
                    break;
                case 3:
                    canvas.drawText("Darker ice bars can float to the side. ", gameFont,860, 400);
                    canvas.drawText("Jump on the ice bar and then knock down the icicle! ", gameFont,860, 340);
                    canvas.drawText("How do you get to higher platforms?", gameFont,2100, 420);
                    canvas.drawText("Try knocking down the icicle!", gameFont,2100, 360);
                    break;
                case 4:
                    canvas.drawText("Control the angle and strength for the throw. ", gameFont,630, 580);
                    canvas.drawText("Don't let penguins fall into the sea! ", gameFont,630, 540);
                    canvas.drawText("If they do, you can still pick them\nback up as they swim towards you!", gameFont, 1120, 420);
                    canvas.drawText("Sometimes it's hard to pick penguins back up...", gameFont,1650, 340);
                    canvas.drawText("So aim at your target carefully.", gameFont,1650, 300);
                    canvas.drawText("You have finished the tutorial levels!", gameFont,2800, 470);
                    canvas.drawText("Go explore the world!", gameFont,2800, 410);
                    break;
                default:
                    break;
            }
        }
        if(complete || failed){
            canvas.draw(assetLoader.blackTexture,new Color(1,1,1,0.1f),cameraX-1280/2,0,3000f,2000f);
        }

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        String noteMsg = "Notes collected: "+ notesCollected + "/"+num_notes;
        String penguinMsg = "Penguins: "+ levelLoader.avatar.getNumPenguins() + "/"+num_penguins;
        if(!complete || failed) {
            canvas.drawText(gameFont, noteMsg, 5.0f+canvas.getCamera().position.x-640, canvas.getHeight() - 5.0f);
            canvas.drawText(gameFont, penguinMsg, 5.0f+canvas.getCamera().position.x-640, canvas.getHeight() - 40.0f);
        }
        if(isPaused){
            canvas.drawFixed(assetLoader.pauseScreen,
                    canvas.getWidth()/2f- 200,
                    canvas.getHeight()/2f-200);
            if(InputController.getInstance().touchUp() && Gdx.input.getX()>500 && Gdx.input.getY()>150&&Gdx.input.getX()<760 && Gdx.input.getY()<280){
                isPaused = false;
                disableMovement = false;
                canvas.end();
                listener.updateScreen(this, GAMEPLAY_CONTINUE);
                return;
            }else if(InputController.getInstance().touchUp() &&Gdx.input.getX()>500 && Gdx.input.getY()>300&&Gdx.input.getX()<760 && Gdx.input.getY()<350){
                isPaused = false;
                disableMovement = false;
                reset();
            }else if(InputController.getInstance().touchUp() &&Gdx.input.getX()>500 && Gdx.input.getY()>370&&Gdx.input.getX()<760 && Gdx.input.getY()<410){
                isPaused = false;
                disableMovement = false;
                for(int i=0;i<7;i++){
                    assetLoader.BackgroundMusic[i].stop();
                }
                canvas.end();
                listener.updateScreen(this, GAMEPLAY_MENU);
                return;
            }
        }
        if(isEditingView){
            canvas.drawSquare(Color.BLACK,1200,560,60,40);
            canvas.drawText(gameFont, "Edit", 1200,600);
        }else{
            canvas.drawFixed(assetLoader.pauseButton,quitPos.x, quitPos.y);
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
            if(!endSoundPlaying) {
                for(int i=0;i<7;i++)
                    assetLoader.BackgroundMusic[i].stop();
                assetLoader.winning.play(0.5f, 1, 0);
                endSoundPlaying = true;
            }
            gameFont.setColor(Color.WHITE);
            canvas.drawFixed(assetLoader.victoryStrip, 460, 300);
            gameFont.setColor(Color.BLACK);
            canvas.end();
        } else if (failed && !resetClick) {
            disableMovement = true;
            levelLoader.avatar.setLinearVelocity(forceCache.set(0,levelLoader.avatar.getLinearVelocity().y));
            canvas.begin(); // DO NOT SCALE
            if(!endSoundPlaying) {
                for(int i=0;i<7;i++)
                    assetLoader.BackgroundMusic[i].stop();
                assetLoader.losing.play(0.5f, 1, 0);
                endSoundPlaying = true;
            }
            gameFont.setColor(Color.WHITE);
            canvas.drawFixed(assetLoader.deadStrip, 460, 200);
            gameFont.setColor(Color.BLACK);
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
            if ((levelLoader.avatar.getSensorName().equals(fd2) && levelLoader.avatar != bd1 && bd1IsGround) ||
                    (levelLoader.avatar.getSensorName().equals(fd1) && levelLoader.avatar != bd2 && bd2IsGround)) {
                levelLoader.avatar.setGrounded(true);
                playerGround += 1;

                if(levelLoader.avatar.moveState == Player.animationState.jumpHanging ||
                        levelLoader.avatar.moveState == Player.animationState.jumpRising){
                    levelLoader.avatar.moveState = Player.animationState.jumpLanding;
                    levelLoader.avatar.setFilmStrip(assetLoader.jumpLandingStrip);
                    assetLoader.bearLanding.play();
                }
                sensorFixtures.add(levelLoader.avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // check whether the penguin is grounded
            bd1IsGround = !(bd1 instanceof Note) && (!(bd1 instanceof Water));
            bd2IsGround = !(bd2 instanceof Note) && (!(bd2 instanceof Water));
            for(Penguin p: levelLoader.avatar.getPenguins()){
                if ((p.getSensorName().equals(fd2) && p != bd1 && bd1 != levelLoader.avatar && bd1IsGround) ||
                        (p.getSensorName().equals(fd1) && p != bd2 && bd2 != levelLoader.avatar && bd2IsGround)) {
                    p.setGrounded(true);
                    if(p.isThrowOut() && p.getBodyType()== BodyDef.BodyType.DynamicBody){
                        if(p.getSoundPlaying())
                            assetLoader.penguinLanding.play();
                        p.setSoundPlaying(false);
                    }
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if(bd1.getName().startsWith("snow") && bd2.getName().startsWith("icicle")){
                WorldManifold worldmanifold;
                worldmanifold = contact.getWorldManifold();
                for(Vector2 point : worldmanifold.getPoints()){
                    if (point.x == bd2.getX()){
                        int index = Integer.parseInt(bd2.getName().substring(bd2.getName().length()-1));
                        staticBodies.set(index, staticBodies.get(index)+1);
                    }
                }
            }
            if(bd2.getName().startsWith("snow") && bd1.getName().startsWith("icicle")){
                WorldManifold worldmanifold;
                worldmanifold = contact.getWorldManifold();
                for(Vector2 point : worldmanifold.getPoints()){
                    if (point.x == bd1.getX()){
                        int index = Integer.parseInt(bd1.getName().substring(bd1.getName().length()-1));
                        staticBodies.set(index, staticBodies.get(index)+1);
                    }
                }

            }


            if (bd1 instanceof Penguin && bd2.getName().startsWith("icicle")){
                icicles_hit.set(levelLoader.iciclesList.indexOf(bd2),true);
            }

            if (bd2 instanceof Penguin && bd1.getName().startsWith("icicle")){
                icicles_hit.set(levelLoader.iciclesList.indexOf(bd1),true);
            }

            // set the ice bar tilt only for levelLoader.avatar
            if ((bd1.getName()=="iceBar" || bd1.getName()=="floatingIceBar") &&
                    (bd2 == levelLoader.avatar || bd2.getName().startsWith("icicle")) ) {
                bd1.setFixedRotation(false);
            }
            if ((bd1 == levelLoader.avatar || bd1.getName().startsWith("icicle")) &&
                    (bd2.getName()=="iceBar"|| bd2.getName()=="floatingIceBar")) {
                bd2.setFixedRotation(false);
            }


            // Check for win condition
            if ((bd1.getName() == "exit" && bd2 == levelLoader.avatar && notesCollected == num_notes) ||
                    (bd1 == levelLoader.avatar && bd2.getName() == "exit" && notesCollected == num_notes)) {
                setComplete(true);
            }


            if((bd1.getName() == bd2.getName()) && bd1.getName() == "floatingIceBar"){
                FloatingIce master1 = (FloatingIce)((BoxObstacle)bd1).getMaster();
                FloatingIce master2 = (FloatingIce)((BoxObstacle)bd2).getMaster();
                if ( master1.getMomentum()>0){
                    master2.hitByFloatingIce(master1.getMomentum()/2, master1.getDirection());
                    master1.hitByFloatingIce(master1.getMomentum()/4,master1.getDirection());
                }
                else {
                    master1.hitByFloatingIce(master2.getMomentum()/2, master2.getDirection());
                    master2.hitByFloatingIce(master2.getMomentum()/4, master2.getDirection());
                }

            }
            else {


                //contact for floating ice bar
                if (bd1.getName() == "floatingIceBar") {
                    ComplexObstacle master = ((BoxObstacle) bd1).getMaster();
                    if (bd2.getName().startsWith("icicle") && bd2.getMass() != 0) {
                        float force = (float) Math.log(bd2.getMass()) / 75;
                        if (bd2.getX() < bd1.getX()) {
                            force = -force;
                        }
                        ((FloatingIce) master).hitByIcicle(force);
                    } else if (!(bd2 instanceof Penguin) && !(bd2 instanceof Player)) {
                        WorldManifold worldmanifold;
                        worldmanifold = contact.getWorldManifold();
                        if (bd1.getX() < worldmanifold.getPoints()[0].x)
                            ((FloatingIce) master).offsetX(1);
                        else
                            ((FloatingIce) master).offsetX(-1);
                    }

                }

                if (bd2.getName() == "floatingIceBar") {
                    ComplexObstacle master = ((BoxObstacle) bd2).getMaster();
                    if (bd1.getName().startsWith("icicle") && bd1.getMass() != 0) {
                        float force = (float) Math.log(bd1.getMass()) / 75;
                        if (bd1.getX() < bd2.getX()) {
                            force = -force;
                        }
                        ((FloatingIce) master).hitByIcicle(force);
                    } else if (!(bd1 instanceof Penguin) && !(bd1 instanceof Player)) {
                        WorldManifold worldmanifold;
                        worldmanifold = contact.getWorldManifold();
                        if (bd2.getX() < worldmanifold.getPoints()[0].x)
                            ((FloatingIce) master).offsetX(1);
                        else
                            ((FloatingIce) master).offsetX(-1);
                    }
                }
            }

            //contact for moving ice bar
            if(bd1.getName() == "movingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
                if(bd2.getName().startsWith("sealion") ){
                    Sealion m = (Sealion) bd2;
                    ((MovingIce) master).addMonster(m);
                }
                else if (bd2 instanceof Player){
                    Player p = (Player) bd2;
                    ((MovingIce) master).addPlayer(p);
                }
                else  if (! (bd2 instanceof Penguin)){
                    WorldManifold worldmanifold;
                    worldmanifold = contact.getWorldManifold();
                    if(bd1.getX()<worldmanifold.getPoints()[0].x) {
                        ((MovingIce) master).hitSomething(1);
                    }
                    else
                        ((MovingIce) master).hitSomething(-1);
                }

            }

            if(bd2.getName() == "movingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd2).getMaster();
                if(bd1.getName().startsWith("sealion") ){
                    Sealion m = (Sealion) bd1;
                    ((MovingIce) master).addMonster(m);
                }
                else if (bd1 instanceof Player){
                    Player p = (Player) bd1;
                    ((MovingIce) master).addPlayer(p);
                }
                else  if (! (bd1 instanceof Penguin)){
                    WorldManifold worldmanifold;
                    worldmanifold = contact.getWorldManifold();

                    if(bd2.getX()<worldmanifold.getPoints()[0].x) {
                        ((MovingIce) master).hitSomething(1);
                    }
                    else
                        ((MovingIce) master).hitSomething(-1);
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
        if ((levelLoader.avatar.getSensorName().equals(fd2) && levelLoader.avatar != bd11 && bd1IsGround) ||
                (levelLoader.avatar.getSensorName().equals(fd1) && levelLoader.avatar != bd22 && bd2IsGround)) {
            sensorFixtures.remove(levelLoader.avatar == bd11 ? fix2 : fix1);
            playerGround -= 1;
            if (playerGround == 0) {
                levelLoader.avatar.setGrounded(false);
            }
        }

        bd1IsGround = !(bd11 instanceof Note) && (!(bd11 instanceof Water));
        bd2IsGround = !(bd22 instanceof Note) && (!(bd22 instanceof Water));
        for(Penguin p: levelLoader.avatar.getPenguins()){
            if ((p.getSensorName().equals(fd2) && p != bd11 && bd11 != levelLoader.avatar && bd2IsGround) ||
                    (p.getSensorName().equals(fd1) && p != bd22 && bd22 != levelLoader.avatar && bd2IsGround)) {
                sensorFixtures.remove(p == bd11 ? fix2 : fix1);
                p.setGrounded(false);
            }
        }



        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            if(bd1.getName().startsWith("snow") && bd2.getName().startsWith("icicle")){
                WorldManifold worldmanifold;
                worldmanifold = contact.getWorldManifold();
                for(Vector2 point : worldmanifold.getPoints()){
                    if (point.x == bd2.getX()){
                        int index = Integer.parseInt(bd2.getName().substring(bd2.getName().length()-1));
                        staticBodies.set(index, staticBodies.get(index)-1);
                    }
                }
            }
            if(bd2.getName().startsWith("snow") && bd1.getName().startsWith("icicle")){
                WorldManifold worldmanifold;
                worldmanifold = contact.getWorldManifold();
                for(Vector2 point : worldmanifold.getPoints()){
                    if (point.x == bd1.getX()){
                        int index = Integer.parseInt(bd1.getName().substring(bd1.getName().length()-1));
                        staticBodies.set(index, staticBodies.get(index)-1);
                    }
                }
            }

        if(bd1.getName() == "movingIceBar"){
            ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
            if(bd2.getName().startsWith("sealion") ){
                Sealion m = (Sealion) bd2;
                ((MovingIce) master).removeMonster(m);
            }
            else if (bd2 instanceof Player){
                ((MovingIce) master).removePlyaer();
            }

        }

        if(bd2.getName() == "movingIceBar"){
            ComplexObstacle master = ((BoxObstacle)bd2).getMaster();
            if(bd1.getName().startsWith("sealion") ){
                Sealion m = (Sealion) bd1;
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