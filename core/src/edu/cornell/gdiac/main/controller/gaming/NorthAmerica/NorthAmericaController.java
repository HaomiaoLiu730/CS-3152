package edu.cornell.gdiac.main.controller.gaming.NorthAmerica;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.model.Component;
import edu.cornell.gdiac.main.model.Penguin;
import edu.cornell.gdiac.main.model.Player;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;
import java.util.List;

public class NorthAmericaController extends WorldController implements ContactListener, ControllerListener {

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private AssetDirectory internal;
    /** Reference to the character avatar */
    private Player avatar;

    private Texture background;
    private Texture rocketTexture;
    /** The texture for walls and platforms */
    private TextureRegion earthTile;
    private Texture hurricaneTexture;
    private List<Texture> icicleTextures = new ArrayList<>();

    // Physics constants for initialization
    /** Density of non-crate objects */
    private static final float BASIC_DENSITY   = 0.0f;
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
    private static final float ROCKET_X = 45f;
    private static final float ROCKET_Y = 3f;
    private static final float HURRICANE_X = 30f;
    private static final float HURRICANE_Y = 7f;
    private static final int NUM_PENGUIN = 2;

    private boolean hitRocket = false;
    private boolean hitHurricane = false;
    private List<Boolean> hitIcicle = new ArrayList<>();

    /** Cooldown (in animation frames) for punching */
    private static final int PUNCH_COOLDOWN = 100;
    /** Length (in animation frames) for punching */
    private static final int PUNCH_TIME = 30;
    private int punchCooldown = 0;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(16f, 5.0f);

    private Component rocket;
    private Component huricane;
    private List<Component> icicle = new ArrayList<>();

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** The world scale */
    protected Vector2 scale;

    private float prevavatarX = 16;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    // Wall vertices
    private static final float[][] WALLS = {
            {
                16f, 1f, 16f, 0f, 0f, 0f, 0f, 1f
            },
            {
                200f, 1f, 200, 0f, 16f, 0f, 16f, 1f
            }
    };

    /** The outlines of all of the platforms */
    private static final float[][] PLATFORMS = {
            {
                0f, 20f, 3f, 16f, 10f, 14f, 13f, 11f, 19f, 10f, 23f, 7f, 29f, 6.5f, 30f, 4f, 39f, 3f, 40f, 0f, 0f, 0f, 0f, 20f
            }
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
        rocketTexture = internal.getEntry("rocket", Texture.class);
        earthTile = new TextureRegion(internal.getEntry("tile", Texture.class));
        hurricaneTexture = internal.getEntry("hurricane", Texture.class);
        Texture icicleTemp = internal.getEntry("icicle", Texture.class);
        icicleTextures.add(icicleTemp);
        icicleTextures.add(icicleTemp);

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
        Vector2 gravity = new Vector2(world.getGravity() );

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
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth, dheight;

        String wname = "wall";
        for (int ii = 0; ii < WALLS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(WALLS[ii], START_X, START_Y);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(wname+ii);
            addObject(obj);
        }

        String pname = "platform";
        for (int ii = 0; ii < PLATFORMS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(PLATFORMS[ii], START_X, START_Y);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(pname+ii);
            addObject(obj);
        }

        rocket = new Component(ROCKET_X, ROCKET_Y, rocketTexture.getWidth()/scale.x, rocketTexture.getHeight()/scale.y, "rocket");
        FilmStrip rocketFilmStrip = new FilmStrip(rocketTexture, 1,1);
        rocket.setFilmStrip(rocketFilmStrip);
        rocket.setDrawScale(scale);
        rocket.setBodyType(BodyDef.BodyType.StaticBody);
        rocket.setDensity(BASIC_DENSITY);
        rocket.setFriction(BASIC_FRICTION);
        rocket.setRestitution(BASIC_RESTITUTION);
        rocket.setSensor(true);
        rocket.setDrawScale(scale);
        rocket.setName("rocket");
        addObject(rocket);

        huricane = new Component(HURRICANE_X, HURRICANE_Y, hurricaneTexture.getWidth()/scale.x, hurricaneTexture.getHeight()/scale.y, "hurricane");
        FilmStrip huricaneFilmStrip = new FilmStrip(hurricaneTexture, 1,1);
        huricane.setFilmStrip(huricaneFilmStrip);
        huricane.setDrawScale(scale);
        huricane.setBodyType(BodyDef.BodyType.StaticBody);
        huricane.setDensity(BASIC_DENSITY);
        huricane.setFriction(BASIC_FRICTION);
        huricane.setRestitution(BASIC_RESTITUTION);
        huricane.setSensor(true);
        huricane.setDrawScale(scale);
        huricane.setName("huricane");
        addObject(huricane);

        icicle.add(new Component(15f, 7f, icicleTextures.get(0).getWidth()/scale.x,
                icicleTextures.get(0).getHeight()/scale.y, "icicle1"));
        icicle.add(new Component(18f, 7f, icicleTextures.get(0).getWidth()/scale.x,
                icicleTextures.get(1).getHeight()/scale.y, "icicle1"));
        for (int i = 0; i < 2; i++){
            FilmStrip icileFilmStrip = new FilmStrip(icicleTextures.get(i), 1,1);
            icicle.get(i).setFilmStrip(icileFilmStrip);
            icicle.get(i).setDrawScale(scale);
            icicle.get(i).setBodyType(BodyDef.BodyType.StaticBody);
            icicle.get(i).setDensity(BASIC_DENSITY);
            icicle.get(i).setFriction(BASIC_FRICTION);
            icicle.get(i).setRestitution(BASIC_RESTITUTION);
            icicle.get(i).setSensor(true);
            icicle.get(i).setDrawScale(scale);
            icicle.get(i).setName("icicle" + i);
            addObject(icicle.get(i));
            hitIcicle.add(false);
        }


        // Create player
        dwidth  = avatarStrip.getRegionWidth()/scale.x;
        dheight = avatarStrip.getRegionHeight()/scale.y;

        avatar = new Player(PLAYER_POS.x, PLAYER_POS.y, dwidth, dheight, NUM_PENGUIN);
        avatar.setDrawScale(scale);
        avatar.setFilmStrip(avatarStrip);
        avatar.setArrowTexture(arrowTexture);
//        avatar.setPenguinWidth(penguinStrip.getRegionWidth());
//        avatar.setPenguinHeight(penguinStrip.getRegionHeight());
        addObject(avatar);

        for(int i = 0; i<NUM_PENGUIN; i++){
            avatar.getPenguins().get(i).setDrawScale(scale);
            avatar.getPenguins().get(i).setFilmStrip(penguinStrip);
            addObject(avatar.getPenguins().get(i));
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
        if(rocket.getY() > 22){
            listener.updateScreen(this, 1);
        }
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
        if(hitHurricane){
            listener.updateScreen(this, 3);
        }
        float moveX = -avatar.getX() + prevavatarX;
        if(Math.abs(moveX) < 1e-2) moveX = 0;
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.setThrowing(InputController.getInstance().didSecondary());
        avatar.setInteract(InputController.getInstance().didXPressed());
        for(Obstacle obj: objects){
            if(obj instanceof Player || obj instanceof Penguin){
                continue;
            }
            if(obj instanceof Component){
                obj.setX(obj.getX()+moveX);
                if(hitRocket && obj.getName() == "rocket"){
                    obj.setY(obj.getY()+0.1f);
                    avatar.setY(rocket.getY()+2);
                    avatar.setX(rocket.getX());
                }
                for (int i = 0; i < icicle.size(); i++){
                    //System.out.println(""+hitIcicle.get(i) + (obj.getName()) + ("icicle" + i));
                    if (hitIcicle.get(i) && (obj.getName().equals("icicle" + i))){
                        while (obj.getY() > 3){
                            //obj.setLinearVelocity(new Vector2(0f, -0.1f));
                            obj.setY(obj.getY() - 0.1f);
                        }
                    }
                }
                continue;
            }
            obj.getBody().setTransform(obj.getX()+moveX, 0, 0);
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

    public void hitRocket(boolean value){
        hitRocket = value;
    }

    public void hitHurricane(boolean value){
        hitHurricane = value;
    }

    public void hitIcicle(int i, boolean value){
        hitIcicle.set(i, value);
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
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }
            for(Penguin p: avatar.getPenguins()){
                // See if we have landed on the ground.
                if ((p.getSensorName().equals(fd2) && p != bd1) ||
                        (p.getSensorName().equals(fd1) && p != bd2)) {
                    p.setGrounded(true);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
                // Check if penguin hit icicle
                if (p.getSensorName().equals(fd1) && icicle.contains(bd2)){
                    hitIcicle(bd2.getName().charAt(bd2.getName().length() - 1)-48, true);
                    //System.out.println(hitIcicle);
                } else if (icicle.contains(bd1) && p.getSensorName().equals(fd2)){
                    hitIcicle(bd1.getName().charAt(bd1.getName().length() - 1)-48, true);
                    //System.out.println(hitIcicle);
                }

            }
            // Check for win condition
            if ((bd1 == avatar   && bd2 == rocket) ||
                    (bd1 == rocket && bd2 == avatar)) {
                hitRocket(true);
            }
            if ((bd1 == avatar   && bd2 == huricane) ||
                    (bd1 == huricane && bd2 == avatar)) {
                hitHurricane(true);
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

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

        for(Penguin p: avatar.getPenguins()){
            if ((p.getSensorName().equals(fd2) && p != bd1) ||
                    (p.getSensorName().equals(fd1) && p != bd2)) {
                sensorFixtures.remove(p == bd1 ? fix2 : fix1);
                if (sensorFixtures.size == 0) {
                    p.setGrounded(false);
                }
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
