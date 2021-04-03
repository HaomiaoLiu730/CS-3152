package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.main.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

import static com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody;

public class Note extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float PLAYER_DENSITY = 50.0f;
    /** The factor to multiply by the input */
    private static final float PLAYER_FORCE = 20.0f;
    /** The dude is a slippery one */
    private static final float PLAYER_FRICTION = 5.0f;

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float PLAYER_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float PLAYER_HSHRINK = 0.8f;

    private FilmStrip filmStrip;
    private float timeCounter = 0;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return PLAYER_FORCE;
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Note(float x, float y, float width, float height, String name) {
        super(x,y,width* PLAYER_HSHRINK,height* PLAYER_VSHRINK);
        setDensity(PLAYER_DENSITY);
        setFriction(PLAYER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setRestitution(0f);
        setFixedRotation(true);
        setBodyType(StaticBody);
        setSensor(true);
        setName(name);
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        return true;
    }

    public void setFilmStrip(FilmStrip strip){
        this.filmStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns
        timeCounter += dt;
        if(timeCounter >= 0.1 && Math.abs(getVX()) > 1e-1) {
            timeCounter = 0;
            filmStrip.nextFrame();
        }
        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(filmStrip,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1f, 1f);
    }
}
