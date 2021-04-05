package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;

public class GameplayController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private AssetDirectory internal;
    /** Reference to the character avatar */
    private Player avatar;
    private Monster monster;
    private PolygonObstacle icicle;
    private ArrayList<Note> notes;
    private ArrayList<Integer> notesCollected = new ArrayList<>();
    private Water water;
    private Ice ice;

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
    /** Threshold for generating sound on collision */
    private static final int NUM_PENGUIN = 2;

    private int playerGround = 0;
    private boolean hitWater = false;
    private boolean levelComplete = false;

    /** Cooldown (in animation frames) for punching */
    private static final int PUNCH_COOLDOWN = 100;
    /** Length (in animation frames) for punching */
    private static final int PUNCH_TIME = 30;
    private int punchCooldown = 0;
    private int resetCountdown = 30;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(16f, 5.0f);

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;
    private float cameraX;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** The outlines of snow lands */
    private static final float[][] SNOW = {
            {200f,1f,200,0f,0f,0f,0f,1f},
            {18f,5f,18f,0f,0f,0f,0f,5f},
            {23f,3f,23f,0f,18f,0f,18f,3f},
            {36f,6f,36f,0f,33.7f,0f,33.7f,6f},
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
        notesCollected = new ArrayList<>();
        levelComplete = false;
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
        resetCountdown = 30;

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

        notes = new ArrayList<>();
        notes.add(new Note(20f, 4f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, "note1"));
        notes.add(new Note(28.35f, 6f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, "note2"));
        for (Note n: notes){
            n.setFilmStrip(noteLeftStrip);
            n.setDrawScale(scale);
            addObject(n);
        }

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

        if (!isFailure() && avatar.getY() < -1) {
            setFailure(true);
            return false;
        }

        return true;
    }

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    @Override
    public void update(float dt) {

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
        if (levelComplete){
            reset();
        }
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
        float dist = avatar.getPosition().dst(monster.getPosition());
        if (avatar.isPunching()) {
            if (dist < 3) {
                objects.remove(monster);
                monster.setActive(false);
                monster.setAwake(false);
            }
        }
        // Monster moving and attacking
        if (monster.isActive()) {
            boolean moveMon = true;
            for(Penguin p: avatar.getPenguins()){
                float dist2 = p.getPosition().dst(monster.getPosition());
                if (dist2 < 3 && dist2 < dist) {
                    monster.setFilmStrip(attackStrip);
                    if (p.getPosition().x < monster.getPosition().x) {
                        monster.setFacingRight(-1);
                    }
                    moveMon = false;
                    resetCountdown -= 1;
                }
            }
            if (moveMon) {
                monster.applyForce();
            }
        }
        // Losing condition
        if(hitWater || resetCountdown<=0){
            reset();
        }
        // Player moving
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        if(avatar.isJumping()&&InputController.getInstance().didPrimary()){
            avatar.moveState = Player.animationState.jumpRising;
            avatar.setFilmStrip(jumpRisingStrip);
        }
        avatar.setThrowing(InputController.getInstance().getClickX(),
                InputController.getInstance().getClickY(),
                InputController.getInstance().touchUp(),
                InputController.getInstance().isTouching());
        avatar.pickUpPenguins();
        for(Obstacle obj: objects){
            if(obj instanceof Monster){
                if (icicle.getPosition().dst(obj.getPosition()) <= 1){
                        objects.remove(monster);
                        monster.setActive(false);
                        monster.setAwake(false);
                }
            }

            if(obj.getName() == "icicle"){
                for (Penguin p: avatar.getPenguins()){
                    dist = p.getPosition().dst(obj.getPosition());
                    if (dist < 2){
                        icicle.setBodyType(BodyDef.BodyType.DynamicBody);
                    }
                }
            }
            if(obj instanceof Water){
                obj.setActive(false);
                float leftX = obj.getX()-((Water) obj).getWidth()/2;
                float rightX = obj.getX()+((Water) obj).getWidth()/2;
                float downY = obj.getY()-((Water) obj).getHeight()/2;
                float upY = obj.getY()+((Water) obj).getHeight()/2;
                if (avatar.getX() >= leftX && avatar.getX() <= rightX && avatar.getY() >= downY && avatar.getY() <= upY) {
                    hitWater(true);
                }
            }
        }
        avatar.applyForce();
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
        canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth()*10,canvas.getHeight()*10);

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        String noteMsg = "Notes collected: "+ notesCollected.size() + "/2";
        String penguinMsg = "Penguins: "+ avatar.getNumPenguins() + "/"+NUM_PENGUIN;
        canvas.drawText( gameFont, noteMsg,5.0f+cameraX-640, canvas.getHeight()-5.0f);
        canvas.drawText( gameFont, penguinMsg,5.0f+cameraX-640, canvas.getHeight()-40.0f);

        canvas.end();

        if (isDebug()) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    public void hitWater(boolean value){
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

            for(Penguin p: avatar.getPenguins()){
                if ((p.getSensorName().equals(fd2) && p != bd1 && bd1 != avatar) ||
                        (p.getSensorName().equals(fd1) && p != bd2 && bd2 != avatar)) {
                    p.setGrounded(true);
//                    p.setFilmStrip(penguinWalkingStrip);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if (bd1.getName()=="iceBar" && bd2 == avatar) {
                bd1.setFixedRotation(false);
            }
            if (bd1 == avatar && bd2.getName()=="iceBar") {
                bd2.setFixedRotation(false);
            }

            if (bd1 instanceof Note && (bd2 instanceof Penguin || bd2 == avatar)){
                int noteHit = bd1.getName().charAt(bd1.getName().length() - 1) - 48;
                ((Note) bd1).setFilmStrip(noteCollectedStrip);
                if (!notesCollected.contains(noteHit)){
                    notesCollected.add(noteHit);
                }
            }
            if (bd2 instanceof Note && (bd1 instanceof Penguin || bd1 == avatar)){
                int noteHit = bd2.getName().charAt(bd2.getName().length() - 1) - 48;
                ((Note) bd2).setFilmStrip(noteCollectedStrip);
                if (!notesCollected.contains(noteHit)){
                    notesCollected.add(noteHit);
                }
            }

            // Check for win condition
            if ((bd1.getName() == "exit" && bd2 == avatar && avatar.getNumPenguins() == NUM_PENGUIN && notesCollected.size() == notes.size()) ||
                    (bd1 == avatar && bd2.getName() == "exit" && avatar.getNumPenguins() == NUM_PENGUIN && notesCollected.size() == notes.size())) {
                levelComplete = true;
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
