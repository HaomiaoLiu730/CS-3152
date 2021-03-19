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
import edu.cornell.gdiac.main.model.*;
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
    private Monster monster;
    private Icicle icicle;

    private Texture background;
    private Texture waterTexture;
    /** The texture for walls and platforms */
    private TextureRegion snow;
    private TextureRegion ice;


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
    private static final float WATER1_X = 2.4f;
    private static final float WATER1_Y = 0f;
    private static final int NUM_PENGUIN = 2;

    private boolean hitWater = false;
    private boolean hitIce = false;
    private boolean hitIcicle = false;

    /** Cooldown (in animation frames) for punching */
    private static final int PUNCH_COOLDOWN = 100;
    /** Length (in animation frames) for punching */
    private static final int PUNCH_TIME = 30;
    private int punchCooldown = 0;

    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(3f, 5.0f);

    private Component waterComponent;

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

    private static final float[][] SNOW = {
            {
                    200f, 1f, 200, 0f, 16f, 0f, 16f, 1f
            },
            {
                21f, 5f, 21f, 0f, 17f,0f,17f,5f
            },
            {27f,3f,27f,0f,21f,0f,21f,3f},
            {40f,5f,40f,0f,37.7f,0f,37.7f,5f},
            {100f,30f,100f,8f,30f,8f,30f,30f},


    };
    private static final float[][] ICE = {
            {
                    35f, 3f, 35f, 0f, 30f,0f,30f,3f
            },



    };

//    private static final float[][] WATER = {
//            {37f,2f,37f,0f,27f,0f,27f,2f},
//    };


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
        ice = new TextureRegion(internal.getEntry("ice", Texture.class));
        waterTexture = internal.getEntry("water", Texture.class);

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
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth, dheight;

        String iname="ice";
        for (int ii=0; ii< ICE.length;ii++){
            PolygonObstacle obj;
            obj = new PolygonObstacle(ICE[ii], START_X, START_Y);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(ice);
            obj.setName(iname+ii);
            addObject(obj);
        }


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

        waterComponent = new Component(WATER1_X,WATER1_Y, waterTexture.getWidth()/scale.x,waterTexture.getHeight()/scale.y, "water");
        FilmStrip waterFilmStrip = new FilmStrip(waterTexture, 1  ,1);
        waterComponent.setFilmStrip(waterFilmStrip);
        waterComponent.setDrawScale(scale);
        waterComponent.setBodyType(BodyDef.BodyType.StaticBody);
        waterComponent.setDensity(BASIC_DENSITY);
        waterComponent.setFriction(BASIC_FRICTION);
        waterComponent.setRestitution(BASIC_RESTITUTION);
        waterComponent.setSensor(true);
        waterComponent.setDrawScale(scale);
        waterComponent.setName("water");
        addObject(waterComponent);

        // Create player
        dwidth  = avatarStrip.getRegionWidth()/scale.x;
        dheight = avatarStrip.getRegionHeight()/scale.y;

        avatar = new Player(PLAYER_POS.x, PLAYER_POS.y, dwidth, dheight, 2);
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

        monster = new Monster(2.5f, 5f, monsterStrip.getRegionWidth()/scale.x, monsterStrip.getRegionHeight()/scale.y, "monster");
        monster.setFilmStrip(monsterStrip);
        monster.setDrawScale(scale);
        addObject(monster);

        icicle = new Icicle(6f, 6.75f, icicleStrip.getRegionWidth()/scale.x, icicleStrip.getRegionHeight()/scale.y, "icicle");
        icicle.setFilmStrip(icicleStrip);
        icicle.setDrawScale(scale);
        addObject(icicle);
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

        //TODO: waterComponent.setFilmStrip(waterFilmStrip);
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
        if (avatar.isPunching()) {
            float dist = avatar.getPosition().dst(monster.getPosition());
                    if (dist < 2) {
                        objects.remove(monster);
                        monster.setActive(false);
                        monster.setAwake(false);
                    }
        }



        if(hitWater){
            reset();
        }
        float moveX = -avatar.getX() + prevavatarX;
        if(Math.abs(moveX) < 1e-2) moveX = 0;
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        if(InputController.getInstance().didPrimary()){
            avatar.setFilmStrip(jumpStrip);
        }
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.setThrowing(InputController.getInstance().didSecondary());
        avatar.setInteract(InputController.getInstance().didXPressed());
        for(Obstacle obj: objects){
            if(obj instanceof Player || obj instanceof Penguin){
                continue;
            }
            if(obj instanceof  Monster){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                continue;
            }
            if(obj instanceof  Icicle){
                obj.getBody().setTransform(obj.getX()+moveX, obj.getY(), 0);
                if (! hitIcicle) obj.setActive(false);
                for (Penguin p: avatar.getPenguins()){
                    float dist = p.getPosition().dst(obj.getPosition());

                    if (dist < 0.8){
                        hitIcicle = true;
                    }
                }
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
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                avatar.setFilmStrip(avatarStrip);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }
            for(Penguin p: avatar.getPenguins()){
                // See if we have landed on the ground.
                if ((p.getSensorName().equals(fd2) && p != bd1) ||
                        (p.getSensorName().equals(fd1) && p != bd2)) {
                    p.setGrounded(true);
                    sensorFixtures.add(p == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }
            if ((monster.getSensorName().equals(fd2) && monster != bd1) ||
                    (monster.getSensorName().equals(fd1) && monster != bd2)) {
                monster.setGrounded(true);
                sensorFixtures.add(monster == bd1 ? fix2 : fix1); // Could have more than one ground
            }


            // Check for win condition
            if ((bd1 == avatar   && bd2 == waterComponent) ||
                    (bd1 == waterComponent && bd2 == avatar)) {
                hitWater(true);
            }

            if ((bd1 == icicle   && bd2 == waterComponent) ||
                    (bd1 == waterComponent && bd2 == icicle)) {
                icicle.setActive(false);
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
