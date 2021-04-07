package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.util.ScreenListener;

public class GameplayController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private AssetDirectory internal;
    /** Reference to the character avatar */
    private Player avatar;
    /** Reference to the monster */
    private Monster monster;
    /** Reference to the icicle */
    private PolygonObstacle icicle;
    /** Reference to the water */
    private Water water;
    /** Reference to the ice */
    private Ice ice;
    /** number of notes collected*/
    public static int notesCollected;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController collisionController;

    private Texture background;
    private TextureRegion snow;
    private BitmapFont gameFont ;
    private TextureRegion iceTextureRegion;

    // Physics constants for initialization
    /** Density of non-crate objects */
    private static final float BASIC_DENSITY   = 2.65f;
    /** Density of the crate objects */
    private static final float CRATE_DENSITY   = 1.0f;
    /** Friction of non-crate objects */
    private static final float BASIC_FRICTION  = 0.3f;
    /** Friction of the crate objects */
    private static final float CRATE_FRICTION  = 0.3f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0.1f;
    /** number of penguins */
    private static final int NUM_PENGUIN = 2;
    /** number of notes */
    private static final int NUM_NOTES = 2;

    private int playerGround = 0;
    private static boolean hitWater = false;
    private boolean complete = false;
    private boolean failed = false;

    /** Cooldown (in animation frames) for punching */
    private static final int PUNCH_COOLDOWN = 100;
    /** Length (in animation frames) for punching */
    private static final int PUNCH_TIME = 30;
    private int punchCooldown = 0;
    /** resetCountdown */
    public static int resetCountDown = 30;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(16f, 5.0f);

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;
    /** camera absolute position*/
    private float cameraX;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** The outlines of snow lands */
    private static final float[][] SNOW = {
            {200f,1f,200,0f,0f,0f,0f,1f},
            {18f,4.5f,18f,0f,0f,0f,0f,4.5f},
            {23f,3f,23f,0f,18f,0f,18f,3f},
            {36f,5f,36f,0f,33.7f,0f,33.7f,5f},
            {96f,30f,96f,11f,26f,11f,26f,30f},
    };

    private static final float[][] ICICLE = {
            {-1f,1.85f,1f,1.85f,0,-1.85f},
    };

    /**
     * Creates a new game with a playing field of the given size.
     * <p>
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     *
     * @param width  The width of the game window
     * @param height The height of the game window
     */
    public GameplayController(float width, float height) {
        super(width,height,DEFAULT_GRAVITY);

        scale = super.scale;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);

        internal = new AssetDirectory("NorthAmerica/NorthAmericaMain.json");
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        snow = new TextureRegion(internal.getEntry("snow", Texture.class));
        iceTextureRegion = new TextureRegion(internal.getEntry("ice", Texture.class));
        gameFont = internal.getEntry("gameFont", BitmapFont.class);

        collisionController = new CollisionController(width, height);
        sensorFixtures = new ObjectSet<Fixture>();
    }

    public GameplayController(){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
        resetCountDown = 30;

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
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth, dheight;

        String sname = "snow";
        for (int ii = 0; ii < SNOW.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(SNOW[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(snow);
            obj.setName(sname+ii);
            addObject(obj);
        }

        icicle = new PolygonObstacle(ICICLE[0], 32f, 9.15f);
        icicle.setBodyType(BodyDef.BodyType.StaticBody);
        icicle.setDensity(30f);
        icicle.setFriction(0.5f);
        icicle.setRestitution(0.2f);
        icicle.setDrawScale(scale);
        icicle.setTexture(icicleStrip);
        icicle.setName("icicle");
        addObject(icicle);

        BoxObstacle exit;
        exit = new BoxObstacle(48, 1.9f, 2, 2);
        exit.setBodyType(BodyDef.BodyType.StaticBody);
        exit.setSensor(true);
        exit.setDensity(BASIC_DENSITY);
        exit.setFriction(BASIC_FRICTION);
        exit.setRestitution(BASIC_RESTITUTION);
        exit.setDrawScale(scale);
        exit.setName("exit");
        exit.setTexture(exitStrip);
        addObject(exit);

        // Create player
        dwidth  = avatarStrip.getRegionWidth()/scale.x;
        dheight = avatarStrip.getRegionHeight()/scale.y;

        avatar = new Player(PLAYER_POS.x, PLAYER_POS.y, dwidth, dheight, 2);
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

        for(int i = 0; i<NUM_PENGUIN; i++){
            avatar.getPenguins().get(i).setDrawScale(scale);
            avatar.getPenguins().get(i).setWalkingStrip(penguinWalkingStrip);
            avatar.getPenguins().get(i).setRolllingFilmStrip(penguinRollingStrip);
            addObject(avatar.getPenguins().get(i));
            avatar.getPenguins().get(i).getBody().setType(BodyDef.BodyType.DynamicBody);
            avatar.getPenguins().get(i).setFilmStrip(penguinWalkingStrip);
        }

        monster = new Monster(28.3f, 3.5f, monsterStrip.getRegionWidth()/scale.x, monsterStrip.getRegionHeight()/scale.y, "monster", 80);
        monster.setFilmStrip(monsterStrip);
        monster.setDrawScale(scale);
        addObject(monster);

        Note note1 = new Note(20f, 4f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, 1);
        note1.setFilmStrip(noteLeftStrip);
        note1.setDrawScale(scale);
        addObject(note1);
        Note note2 = new Note(28.35f, 6f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, 2);
        note2.setFilmStrip(noteLeftStrip);
        note2.setDrawScale(scale);
        addObject(note2);

        water = new Water(28.35f, 1.9f, waterStrip.getRegionWidth()/scale.x, waterStrip.getRegionHeight()/scale.y, "water");
        water.setActive(false);
        water.setFilmStrip(waterStrip);
        water.setDrawScale(scale);
        addObject(water);

        dwidth  = iceTextureRegion.getRegionWidth()/scale.x;
        dheight = iceTextureRegion.getRegionHeight()/scale.y;
        ice = new Ice(28.35f,3.2f,dwidth,dheight);
        ice.setDrawScale(scale);
        ice.setTexture(iceTextureRegion);
        ice.setRestitution(0);
        addObject(ice);
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
        internal.unloadAssets();
        internal.dispose();
        collisionController = null;
        canvas = null;
    }

    @Override
    public void update(float dt) {

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
            setDebug(true);
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
        if(hitWater || resetCountDown <=0){
            setFailure(true);
            setComplete(true);
        }

        // Monster moving and attacking
        collisionController.processCollision(monster, avatar, objects);
        collisionController.processCollision(monster, attackStrip, avatar.getPenguins());
        collisionController.processCollision(monster, icicle, objects);
        collisionController.processCollision(avatar.getPenguins(), icicle, objects);
        collisionController.processCollision(water, avatar);
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
                InputController.getInstance().touchUp(),
                InputController.getInstance().isTouching());
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
        canvas.clear();

        canvas.begin();
        canvas.drawBackground(background,0, -100);

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        String noteMsg = "Notes collected: "+ notesCollected + "/"+NUM_NOTES;
        String penguinMsg = "Penguins: "+ avatar.getNumPenguins() + "/"+NUM_PENGUIN;
        canvas.drawText( gameFont, noteMsg,5.0f, canvas.getHeight()-5.0f);
        canvas.drawText( gameFont, penguinMsg,5.0f, canvas.getHeight()-40.0f);
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

            // check whether the penguin is grounded
            for(Penguin p: avatar.getPenguins()){
                if ((p.getSensorName().equals(fd2) && p != bd1 && bd1 != avatar) ||
                        (p.getSensorName().equals(fd1) && p != bd2 && bd2 != avatar)) {
                    p.setGrounded(true);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            // set the ice bar tilt only for avatar
            if (bd1.getName()=="iceBar" && bd2 == avatar) {
                bd1.setFixedRotation(false);
            }
            if (bd1 == avatar && bd2.getName()=="iceBar") {
                bd2.setFixedRotation(false);
            }

            // check for note collection
            if (bd1 instanceof Note && (bd2 instanceof Penguin || bd2 == avatar)){
                if(!((Note) bd1).isCollected()){
                    ((Note) bd1).setFilmStrip(noteCollectedStrip);
                    ((Note) bd1).setCollected(true);
                    notesCollected++;
                }
            }else if(bd2 instanceof Note && (bd1 instanceof Penguin || bd1 == avatar)){
                if(!((Note) bd2).isCollected()){
                    ((Note) bd2).setFilmStrip(noteCollectedStrip);
                    ((Note) bd2).setCollected(true);
                    notesCollected++;
                }
            }

            // Check for win condition
            if ((bd1.getName() == "exit" && bd2 == avatar && avatar.getNumPenguins() == NUM_PENGUIN && notesCollected == NUM_NOTES) ||
                    (bd1 == avatar && bd2.getName() == "exit" && avatar.getNumPenguins() == NUM_PENGUIN && notesCollected == NUM_NOTES)) {
                setComplete(true);
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

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        boolean bd1IsGround = !(bd1 instanceof Penguin) && !(bd1 instanceof Note) && (!(bd1 instanceof Water));
        boolean bd2IsGround = !(bd2 instanceof Penguin) && !(bd2 instanceof Note) && (!(bd2 instanceof Water));
        if ((avatar.getSensorName().equals(fd2) && avatar != bd1 && bd1IsGround) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2 && bd2IsGround)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            playerGround -= 1;
            if (playerGround == 0) {
                avatar.setGrounded(false);
            }
        }

        for(Penguin p: avatar.getPenguins()){
            if ((p.getSensorName().equals(fd2) && p != bd1 && bd1 != avatar) ||
                    (p.getSensorName().equals(fd1) && p != bd2 && bd2 != avatar)) {
                sensorFixtures.remove(p == bd1 ? fix2 : fix1);
                p.setGrounded(false);
            }
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
