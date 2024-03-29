/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination
 * of the CollisionController and GameplayController from the previous lab.  There is not
 * much to do for collisions; Box2d takes care of all of that for us.  This controller
 * invokes Box2d and then performs any after the fact modifications to the data
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.main.controller;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.main.obstacle.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public abstract class WorldController implements Screen {

    /**
     * Tracks the asset state.  Otherwise subclasses will try to load assets
     */
    protected enum AssetState {
        /** No assets loaded */
        EMPTY,
        /** Still loading assets */
        LOADING,
        /** Assets are complete */
        COMPLETE
    }

    /** Track asset loading from all instances and subclasses */
    protected AssetState worldAssetState = AssetState.EMPTY;
    /** Track all loaded assets (for unloading purposes) */
    protected Array<String> assets;

    /** The texture for the player */
    protected FilmStrip avatarStrip;
    /** The texture for the player */
    protected FilmStrip avatarNormalStrip;
    /** The texture for the penguin */
    protected FilmStrip penguinWalkingStrip;
    /** The texture for the penguin rolling */
    protected FilmStrip penguinRollingStrip;
    protected ArrayList<FilmStrip> penguins = new ArrayList<>();
    /** The texture for the monster */
    protected FilmStrip sealionStrip;
    /** The texture for the monster */
    protected FilmStrip sealStrip;
    /** The texture for the waves */
    protected FilmStrip wavesStrip;
    /** The texture for the water */
    protected FilmStrip waterStrip;
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
    /** The texture for the pure white*/
    protected Texture whiteTexture;
    /** The texture for the pure black*/
    protected Texture blackTexture;
    /** The texture region for the snow */
    protected TextureRegion snowTextureRegion;
    protected TextureRegion pauseButton;
    protected TextureRegion pauseScreen;
    /** The texture region for the ice */
    protected TextureRegion iceTextureRegion;
    /** The texture region for the floating ice */
    protected TextureRegion ficeTextureRegion;
    /** The texture region for the moving ice */
    protected TextureRegion miceTextureRegion;
    /** The texture for the icicle */
    protected FilmStrip icicleStrip;
    /** The texture for the notes not collected */
    protected FilmStrip noteLeftStrip;
    /** The texture for the notes collected */
    protected FilmStrip noteCollectedStrip;
    /** The texture for the exit */
    protected FilmStrip exitStrip;
    protected FilmStrip teachThrowingStrip;
    /** The texture for the exit */
    protected TextureRegion deadStrip;


    /** The texture for the player punching */
    protected FilmStrip punchStrip;
    /** The texture for the player jumping */
    protected FilmStrip jumpRisingStrip;
    /** The texture for the player jumping */
    protected FilmStrip jumpHangingStrip;
    /** The texture for the player jumping */
    protected FilmStrip jumpLandingStrip;
    protected FilmStrip throwingStrip;

    /** The font for giving messages to the player */
    protected BitmapFont displayFont;

    protected Sound hitIcicle;
    protected Sound punching;
    protected Sound jumping;
    protected Sound throwingP;
    protected Sound collectingNote;
    protected Sound menuSellect;
    protected Sound bearLanding;
    protected Sound penguinLanding;
    protected Sound winning;
    protected Sound losing;
    protected Sound[] BackgroundMusic = new Sound[7];


    /**
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param directory Reference to global asset directory.
     */
    public void preLoadContent(AssetDirectory directory) {
        if (worldAssetState != AssetState.EMPTY) {
            return;
        }

        worldAssetState = AssetState.LOADING;
        // Load the shared tiles.
    }

    /**
     * Loads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param directory Reference to global asset manager.
     */
    public void loadContent(AssetDirectory directory) {

//        if (worldAssetState != AssetState.LOADING) {
//            return;
//        }

        // Allocate the tiles
        avatarStrip = new FilmStrip(directory.getEntry("avatarWalking", Texture.class), 1, 18);
        penguins.add(new FilmStrip(directory.getEntry("penguin", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin2", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin3", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin4", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin5", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin6", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin7", Texture.class), 1, 1));
        avatarNormalStrip = new FilmStrip(directory.getEntry("avatarNormal", Texture.class), 1, 1);
        penguinWalkingStrip = new FilmStrip(directory.getEntry("penguinWalking", Texture.class), 1, 29);
        penguinRollingStrip = new FilmStrip(directory.getEntry("penguinRolling", Texture.class), 1, 1);
        sealionStrip = new FilmStrip(directory.getEntry("sealion", Texture.class), 1, 1);
        sealStrip = new FilmStrip(directory.getEntry("seal", Texture.class), 1, 1);
        attackStrip = new FilmStrip(directory.getEntry("monsterAttacking", Texture.class), 1, 2);
        icicleStrip = new FilmStrip(directory.getEntry("icicle", Texture.class), 1, 1);
        exitStrip = new FilmStrip(directory.getEntry("exit", Texture.class), 1, 1);
        arrowTexture = directory.getEntry("arrow", Texture.class);
        energyBarOutlineTexture = directory.getEntry("energyBarOutline", Texture.class);
        energyBarTexture = directory.getEntry("energyBar", Texture.class);
        punchStrip = new FilmStrip(directory.getEntry("avatarPunching", Texture.class), 1, 1);
        noteLeftStrip = new FilmStrip(directory.getEntry("notcollected", Texture.class), 1, 1);
        noteCollectedStrip = new FilmStrip(directory.getEntry("collected", Texture.class), 1, 1);
        jumpRisingStrip = new FilmStrip(directory.getEntry("jumpRising", Texture.class), 1, 4);
        jumpHangingStrip = new FilmStrip(directory.getEntry("jumpHanging", Texture.class), 1, 1);
        jumpLandingStrip = new FilmStrip(directory.getEntry("jumpLanding", Texture.class), 1, 3);
        throwingStrip = new FilmStrip(directory.getEntry("avatarThrow", Texture.class), 1, 6);
        worldAssetState = AssetState.COMPLETE;
        icicleStrip = new FilmStrip(directory.getEntry("icicle", Texture.class), 10, 165);
        iceStrip= new FilmStrip(directory.getEntry("ice", Texture.class), 1, 1);
        snowTextureRegion = new TextureRegion(directory.getEntry("snow", Texture.class));
        iceTextureRegion = new TextureRegion(directory.getEntry("ice", Texture.class));
        waterStrip =new FilmStrip(directory.getEntry("water", Texture.class), 1, 1);
        wavesStrip = new FilmStrip(directory.getEntry("waves", Texture.class), 1, 4);
        ficeTextureRegion = new TextureRegion(directory.getEntry("floatingIce", Texture.class));
        miceTextureRegion = new TextureRegion(directory.getEntry("ice", Texture.class));
        whiteTexture = directory.getEntry("white",Texture.class);
        blackTexture = directory.getEntry("black",Texture.class);
        pauseButton = new TextureRegion(directory.getEntry("pauseButton", Texture.class));
        pauseScreen = new TextureRegion(directory.getEntry("pauseScreen",Texture.class));
        teachThrowingStrip = new FilmStrip(directory.getEntry("teachThrowing", Texture.class), 12, 8);;
        deadStrip = new TextureRegion(directory.getEntry("dead",Texture.class));

        hitIcicle = directory.getEntry("hitIcicle", SoundBuffer.class);
        punching = directory.getEntry("punching", SoundBuffer.class);
        jumping = directory.getEntry("jumping", SoundBuffer.class);
        throwingP = directory.getEntry("throwingP", SoundBuffer.class);
        collectingNote = directory.getEntry("collectingNote", SoundBuffer.class);
        menuSellect = directory.getEntry("menuSellect", SoundBuffer.class);
        bearLanding = directory.getEntry("bearLanding", SoundBuffer.class);
        penguinLanding = directory.getEntry("penguinLanding", SoundBuffer.class);
        winning = directory.getEntry("winning", SoundBuffer.class);
        losing = directory.getEntry("losing",SoundBuffer.class);

        BackgroundMusic[0] = directory.getEntry("l1",SoundBuffer.class);
        BackgroundMusic[1] = directory.getEntry("l2",SoundBuffer.class);
        BackgroundMusic[2] = directory.getEntry("l3",SoundBuffer.class);
        BackgroundMusic[3] = directory.getEntry("l4",SoundBuffer.class);
        BackgroundMusic[4] = directory.getEntry("l5",SoundBuffer.class);
        BackgroundMusic[5] = directory.getEntry("l6",SoundBuffer.class);
        BackgroundMusic[6] = directory.getEntry("l7",SoundBuffer.class);

    }

    /**
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }

    /**
     * Returns a newly loaded filmstrip for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param rows 		The number of rows in the filmstrip
     * @param cols 		The number of columns in the filmstrip
     * @param size 		The number of frames in the filmstrip
     *
     * @return a newly loaded texture region for the given file.
     */
    protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
        if (manager.isLoaded(file)) {
            FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
            strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return strip;
        }
        return null;
    }

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void unloadContent(AssetManager manager) {
        for(String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
    }

    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;

    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = -9.8f;

    // TODO: see this
    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** Queue for adding objects */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether we have completed this level */
    private boolean complete;
    /** Whether we have failed at this world (and need a reset) */
    private boolean failed;
    /** Whether or not debug mode is active */
    private boolean debug;
    /** Countdown active for winning or losing */
    private int countdown;

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive( ) {
        return active;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
    }

    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected WorldController() {
        this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
                new Vector2(0,DEFAULT_GRAVITY));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width  	The width in Box2d coordinates
     * @param height	The height in Box2d coordinates
     * @param gravity	The downward gravity
     */
    protected WorldController(float width, float height, float gravity) {
        this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds	The game bounds in Box2d coordinates
     * @param gravity	The gravitational force on this Box2d world
     */
    protected WorldController(Rectangle bounds, Vector2 gravity) {
        assets = new Array<String>();
        world = new World(gravity,false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        complete = false;
        failed = false;
        debug  = false;
        active = false;
        countdown = -1;
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
        canvas = null;
    }

    /**
     *
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public abstract void reset();

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
        InputController input = InputController.getInstance();
        input.readInput(bounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            listener.updateScreen(this, EXIT_QUIT);
            return false;
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            if (failed) {
                reset();
            } else if (complete) {
                listener.updateScreen(this, EXIT_NEXT);
                return false;
            }
        }
        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public abstract void update(float dt);

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    /**
     * Draw the physics objects to the canvas
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param delta The time
     */
    public void draw(float delta) {
        canvas.clear();

        canvas.begin();
        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        if (debug) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
        canvas.end();
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
                postUpdate(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

}