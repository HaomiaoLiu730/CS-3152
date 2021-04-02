package edu.cornell.gdiac.main.controller.gaming.NorthAmerica;

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

public class NorthAmericaController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private AssetDirectory internal;
    /** Reference to the character avatar */
    private Player avatar;
    private Monster monster;
    private Icicle icicle;
    private ArrayList<Note> notes;
    private ArrayList<Integer> notesCollected = new ArrayList<>();
    private Water water;
    private Ice ice;


    private Texture background;
    private Texture waterTexture;
    /** The texture for walls and platforms */
    private TextureRegion snow;
    private BitmapFont gameFont ;
    private TextureRegion iceTextureRegion;
//    private TextureRegion ice;


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
    private static final float SOUND_THRESHOLD = 1.0f;
    private static final float START_X = -30f;
    private static final float START_Y = 0f;
    private static final float WATER1_X = 2.4f;
    private static final float WATER1_Y = 0f;
    private static final int NUM_PENGUIN = 2;

    private int playerGround = 0;
    private boolean hitWater = false;
    private boolean hitIce = false;
    private boolean hitIcicle = false;
    private boolean levelComplete = false;

    /** Cooldown (in animation frames) for punching */
    private static final int PUNCH_COOLDOWN = 100;
    /** Length (in animation frames) for punching */
    private static final int PUNCH_TIME = 30;
    private int punchCooldown = 0;
    private int resetCountdown = 30;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(3f, 5.0f);

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;

    private float prevavatarX = 16;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** The outlines of snow lands */
    private static final float[][] SNOW = {
            {200f, 1f, 200, 0f, 16f, 0f, 16f, 1f},
            {21f, 5f, 21f, 0f, 17f,0f,17f,5f},
            {27f,3f,27f,0f,21f,0f,21f,3f},
            {40f,5f,40f,0f,37.7f,0f,37.7f,5f},
            {100f,30f,100f,8f,30f,8f,30f,30f},
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
    public NorthAmericaController(float width, float height) {
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
        waterTexture = internal.getEntry("water", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);

        sensorFixtures = new ObjectSet<Fixture>();
    }

    public NorthAmericaController(){
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
        // TODO: load assets
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
        prevavatarX=16;
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
        hitIcicle = false;
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
            obj = new PolygonObstacle(SNOW[ii], START_X, START_Y);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(snow);
            obj.setName(sname+ii);
            addObject(obj);
        }

        BoxObstacle exit;
        exit = new BoxObstacle(20, 1.9f, 3, 3);
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
            avatar.getPenguins().get(i).getBody().setType(BodyDef.BodyType.StaticBody);
            avatar.getPenguins().get(i).setFilmStrip(penguinWalkingStrip);
        }

        monster = new Monster(2.5f, 2.5f, monsterStrip.getRegionWidth()/scale.x, monsterStrip.getRegionHeight()/scale.y, "monster", 80);
        monster.setFilmStrip(monsterStrip);
        monster.setDrawScale(scale);
        addObject(monster);

        icicle = new Icicle(6f, 6.75f, icicleStrip.getRegionWidth()/scale.x, icicleStrip.getRegionHeight()/scale.y, "icicle");
        icicle.setFilmStrip(icicleStrip);
        icicle.setDrawScale(scale);
        addObject(icicle);

        notes = new ArrayList<>();
        notes.add(new Note(-7f, 3.6f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, "note1"));
        notes.add(new Note(3f, 5f, noteLeftStrip.getRegionWidth()/scale.x, noteLeftStrip.getRegionHeight()/scale.y, "note2"));
        for (Note n: notes){
            n.setFilmStrip(noteLeftStrip);
            n.setDrawScale(scale);
            addObject(n);
        }


//        water = new Water(4f, 4f, waterStrip.getRegionWidth()/scale.x, waterStrip.getRegionHeight()/scale.y, "water");
        water = new Water(2.4f, 0.5f, waterStrip.getRegionWidth()/scale.x, waterStrip.getRegionHeight()/scale.y, "water");

        water.setFilmStrip(waterStrip);
        water.setDrawScale(scale);
        addObject(water);

        dwidth  = iceTextureRegion.getRegionWidth()/scale.x;
        dheight = iceTextureRegion.getRegionHeight()/scale.y;
        ice = new Ice(2.5f,1.8f,dwidth,dheight);
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
        if (levelComplete){
            reset();
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
        float moveX = -avatar.getX() + prevavatarX;
        if(Math.abs(moveX) < 1e-2) moveX = 0;
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        if(avatar.isJumping()&&InputController.getInstance().didPrimary()){
            avatar.moveState = Player.animationState.jumpRising;
            avatar.setFilmStrip(jumpRisingStrip);
        }
        avatar.setThrowing(InputController.getInstance().getClickX(),
                InputController.getInstance().getClickY(),
                avatar.getX(),
                avatar.getY(),
                InputController.getInstance().touchUp(),
                InputController.getInstance().isTouching());
        avatar.setInteract(InputController.getInstance().didXPressed());
        for(Obstacle obj: objects){
            if(obj instanceof Player || obj instanceof Penguin){
                continue;
            }
            if(obj instanceof Monster){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                continue;
            }
            if(obj instanceof  Note){ 
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                continue;
            }

            if(obj.getName() == "exit"){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                continue;
            }
            if(obj instanceof  Icicle){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                if (!hitIcicle) obj.setActive(false);
                for (Penguin p: avatar.getPenguins()){
                    dist = p.getPosition().dst(obj.getPosition());

                    if (dist < 0.8){
                        hitIcicle = true;
                    }
                }
                continue;
            }
            if(obj instanceof Ice){
                //obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                ((Ice) obj).setTranform(obj.getX()+moveX, obj.getY(), 0);
//                System.out.println(obj.getX()+" ice ");
                //obj.setActive(false);
                continue;
            }
            if(obj instanceof  Water){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                // obj.setActive(false);
                continue;
            }
            obj.getBody().setTransform(obj.getX()+moveX, 0, 0);

        }

        if(hitIcicle){
            icicle.setActive(true);
            icicle.setAwake(true);
        }

        prevavatarX = avatar.getX();
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
        canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        String noteMsg = "Notes collected: "+ notesCollected.size() + "/2";
        String penguinMsg = "Penguins: "+ avatar.getNumPenguins() + "/"+NUM_PENGUIN;
        canvas.drawText( gameFont, noteMsg,5.0f, canvas.getHeight()-5.0f);
        canvas.drawText( gameFont, penguinMsg,5.0f, canvas.getHeight()-40.0f);

        canvas.end();

//        if (debug) {
//            canvas.beginDebug();
//            for(Obstacle obj : objects) {
//                obj.drawDebug(canvas);
//            }
//            canvas.endDebug();
//        }
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
            boolean bd1IsGround = !(bd1 instanceof Icicle) && !(bd1 instanceof Note) &&
                    !(bd1 instanceof Penguin) && !(bd1 instanceof Water);
            boolean bd2IsGround = !(bd2 instanceof Icicle) && !(bd2 instanceof Note) &&
                    !(bd2 instanceof Penguin) && !(bd2 instanceof Water);

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
                if ((p.getSensorName().equals(fd2) && p != bd1 && bd1IsGround && bd1 != avatar) ||
                        (p.getSensorName().equals(fd1) && p != bd2 && bd2IsGround && bd2 != avatar)) {
                    p.setGrounded(true);
//                    p.getBody().setType(BodyDef.BodyType.StaticBody);
//                    p.setFilmStrip(penguinWalkingStrip);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            // Check for win condition
            if ((bd1 == avatar   && bd2 == water) ||
                    (bd1 == water && bd2 == avatar)) {
                hitWater(true);
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

            if ((bd1.getName() == "exit"   && bd2 == avatar && avatar.getNumPenguins() >= 2) ||
                    (bd1 == avatar && bd2.getName() == "exit" && avatar.getNumPenguins() >= 2)) {
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

        boolean bd1IsGround = !(bd1 instanceof Icicle) && !(bd1 instanceof Note) &&
                !(bd1 instanceof Penguin) && !(bd1 instanceof Water);
        boolean bd2IsGround = !(bd2 instanceof Icicle) && !(bd2 instanceof Note) &&
                !(bd2 instanceof Penguin) && !(bd2 instanceof Water);

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1 && bd1IsGround) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2 && bd2IsGround)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            playerGround -= 1;
            avatar.setGrounded(false);
//            if (playerGround == 0) {
//                avatar.setGrounded(false);
//            }
        }

        for(Penguin p: avatar.getPenguins()){
            if ((p.getSensorName().equals(fd2) && p != bd1 && bd1IsGround && bd1 != avatar) ||
                    (p.getSensorName().equals(fd1) && p != bd2 && bd2IsGround && bd2 != avatar)) {
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
