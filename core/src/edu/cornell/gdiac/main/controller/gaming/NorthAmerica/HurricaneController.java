package edu.cornell.gdiac.main.controller.gaming.NorthAmerica;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.controller.WorldController;
import edu.cornell.gdiac.main.model.Component;
import edu.cornell.gdiac.main.model.Player;
import edu.cornell.gdiac.main.obstacle.Obstacle;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

public class HurricaneController extends WorldController implements ContactListener {

    private ScreenListener listener;
    private AssetDirectory internal;
    private Texture background;
    private TextureRegion earthTile;
    private Texture hurricaneTexture;
    /** The texture for the player */
    protected FilmStrip avatarStrip;

    // Physics constants for initialization
    /** Density of non-crate objects */
    private static final float BASIC_DENSITY   = 0.0f;
    /** Density of the crate objects */
    private static final float CRATE_DENSITY   = 1.0f;
    /** Friction of non-crate objects */
    private static final float BASIC_FRICTION  = 0.8f;
    /** Friction of the crate objects */
    private static final float CRATE_FRICTION  = 0.3f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0.7f;
    /** Threshold for generating sound on collision */
    private static final float SOUND_THRESHOLD = 1.0f;
    private static final float START_X = 0f;
    private static final float START_Y = 0f;
    private static final float HURRICANE_X = 4f;
    private static final float HURRICANE_Y = 7f;
    private static final int NUM_PENGUIN = 1;
    /** The initial position of the player */
    private static Vector2 PLAYER_POS = new Vector2(4f, 5.0f);


    private Component huricane;
    private Player avatar;



    // Wall vertices
    private static final float[][] WALLS = {
            {
                    16f, 1f, 16f, 0f, 0f, 0f, 0f, 1f
            },
            {
                    200f, 1f, 200, 0f, 16f, 0f, 16f, 1f
            }
    };


    public HurricaneController(float width, float height){
        super(width,height,DEFAULT_GRAVITY);

        scale = super.scale;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);

        internal = new AssetDirectory("NorthAmerica/NorthAmericaHurricane.json");
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        earthTile = new TextureRegion(internal.getEntry("tile", Texture.class));
        hurricaneTexture = internal.getEntry("hurricane", Texture.class);
    }

    public HurricaneController(){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    @Override
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

    public void populateLevel(){
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

        // Create player
        dwidth  = avatarStrip.getRegionWidth()/scale.x;
        dheight = avatarStrip.getRegionHeight()/scale.y;

        avatar = new Player(PLAYER_POS.x, PLAYER_POS.y, dwidth, dheight, NUM_PENGUIN);
        avatar.setDrawScale(scale);
        avatar.setFilmStrip(avatarStrip);
        addObject(avatar);
    }

    @Override
    public void update(float dt) {
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
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
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    @Override
    public void beginContact(Contact contact) {

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
