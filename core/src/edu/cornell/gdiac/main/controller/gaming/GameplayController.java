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

        internal = new AssetDirectory("NorthAmerica/northAmericaMain.json");
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);

        collisionController = new CollisionController(width, height);
        sensorFixtures = new ObjectSet<Fixture>();

        constants = internal.getEntry( "level1", JsonValue.class );
        JsonValue defaults = constants.get("defaults");
        num_penguins = defaults.getInt("num_penguins",0);
        num_notes = defaults.getInt("num_notes",0);


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
        JsonValue iciclepos=icicles.get("pos");

        icicle = new PolygonObstacle(icicles.get("layout").get(0).asFloatArray(), iciclepos.getFloat(0), iciclepos.getFloat(1));
        icicle.setBodyType(BodyDef.BodyType.StaticBody);
        icicle.setDensity(icicles.getFloat("density"));
        icicle.setFriction(icicles.getFloat("friction"));
        icicle.setRestitution(icicles.getFloat("restitution"));
        icicle.setDrawScale(scale);
        icicle.setTexture(icicleStrip);
        icicle.setName("icicle");
        addObject(icicle);

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
        JsonValue enemypos = enemy.get("pos");
        for (int i=0; i < enemypos.size; i++) { //multiple monsters
            monster = new Monster(enemy, monsterStrip.getRegionWidth() / scale.x, monsterStrip.getRegionHeight() / scale.y, "monster", enemy.getInt("range"),i);
            monster.setFilmStrip(monsterStrip);
            monster.setDrawScale(scale);
            addObject(monster);
        }
        JsonValue notes = constants.get("notes");
        JsonValue notespos = notes.get("pos");
        for (int i =0; i< notespos.size; i++) {
            Note note = new Note(notes, noteLeftStrip.getRegionWidth() / scale.x, noteLeftStrip.getRegionHeight() / scale.y, i );
            note.setFilmStrip(noteLeftStrip);
            note.setDrawScale(scale);
            addObject(note);
        }

        JsonValue waters = constants.get("water");
        JsonValue waterpos = waters.get("pos");
        for (int i =0; i< waterpos.size; i++) {
            water = new Water(waters, waterStrip.getRegionWidth() / scale.x, waterStrip.getRegionHeight() / scale.y, "water",i);
            water.setActive(false);
            water.setFilmStrip(waterStrip);
            water.setDrawScale(scale);
            addObject(water);
        }
//        JsonValue ices = constants.get("ice");
//        JsonValue icepos = ices.get("pos");
//        dwidth  = iceTextureRegion.getRegionWidth()/scale.x;
//        dheight = iceTextureRegion.getRegionHeight()/scale.y;
//        for (int i =0; i< icepos.size; i++) {
//            ice = new Ice(ices, i, dwidth, dheight);
//            ice.setDrawScale(scale);
//            ice.setTexture(iceTextureRegion);
//            ice.setRestitution(ices.getFloat("restitution"));
//            addObject(ice);
//        }

//        JsonValue fices = constants.get("floatingIce");
//        JsonValue ficepos = fices.get("pos");
//        dwidth  = iceTextureRegion.getRegionWidth()/scale.x;
//        dheight = iceTextureRegion.getRegionHeight()/scale.y;
//        FloatingIce fIce;
//        for (int i =0; i< ficepos.size; i++) {
//            fIce = new FloatingIce(fices, i, dwidth, dheight);
//            fIce.setDrawScale(scale);
//            fIce.setTexture(iceTextureRegion);
//            fIce.setRestitution(fices.getFloat("restitution"));
//            addObject(fIce);
//        }

        JsonValue mices = constants.get("movingIce");
        JsonValue micepos = mices.get("pos");
        dwidth  = iceTextureRegion.getRegionWidth()/scale.x;
        dheight = iceTextureRegion.getRegionHeight()/scale.y;
        MovingIce mIce;
        for (int i =0; i< micepos.size; i++) {
            mIce = new MovingIce(mices, i, dwidth, dheight);
            mIce.setDrawScale(scale);
            mIce.setTexture(iceTextureRegion);
            mIce.setRestitution(mices.getFloat("restitution"));
            addObject(mIce);
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

        String noteMsg = "Notes collected: "+ notesCollected + "/"+num_notes;
        String penguinMsg = "Penguins: "+ avatar.getNumPenguins() + "/"+num_penguins;
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
            if ((bd1.getName()=="iceBar" || bd1.getName()=="floatingIceBar") && bd2 == avatar) {
                bd1.setFixedRotation(false);
            }
            if (bd1 == avatar && (bd2.getName()=="iceBar"|| bd2.getName()=="floatingIceBar")) {
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
            if ((bd1.getName() == "exit" && bd2 == avatar && avatar.getNumPenguins() == num_penguins && notesCollected == num_notes) ||
                    (bd1 == avatar && bd2.getName() == "exit" && avatar.getNumPenguins() == num_penguins && notesCollected == num_notes)) {
                setComplete(true);
            }

            if(bd1.getName() == "floatingIceBar"){
                ComplexObstacle master = ((BoxObstacle)bd1).getMaster();
                if(bd2.getName() == "icicle"){
                    float force = bd2.getMass()/2000;
                    System.out.println(bd2.getMass());
                    System.out.println(force);
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
                if(bd1.getName() == "icicle"){
                    float force = bd1.getMass()/2000;
                    System.out.println(bd2.getMass());
                    System.out.println(force);
                    if (bd1.getX()<bd2.getX()){
                        force = -force;
                    }
                    ((FloatingIce)master).hitByIcicle(force);
                }
                else if (!(bd1 instanceof Penguin)){
                    ((FloatingIce)master).resetMomentum();
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
