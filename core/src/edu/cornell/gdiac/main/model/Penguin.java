package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Penguin extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float PENGUIN_DENSITY = 1.0f;
    /** The factor to multiply by the input */
    private static final float PENGUIN_FORCE = 20.0f;
    /** The amount to slow the character down */
    private static final float PENGUIN_DAMPING = 2.0f;
    /** The dude is a slippery one */
    private static final float PENGUIN_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float PENGUIN_MAXSPEED = 3.0f;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "PenguinGroundSensor";

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float PLAYER_VSHRINK = 0.75f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float PLAYER_HSHRINK = 0.75f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float PLAYER_SSHRINK = 0.6f;

    private int index;


    /** Which direction is the character facing */
    private boolean faceRight;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private FilmStrip filmStrip;
    private FilmStrip walkingStrip;
    private FilmStrip rollingStrip;
    private float timeCounter = 0;
    private boolean isThrownOut;
    private float angle;
    public boolean updateWalking = false;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();
    /** Cache for angle calculations */
    private Vector2 temp = new Vector2();

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param force force movement of this character.
     * @param xDir x movement of this character.
     */
    public void setMovement(float force, float xDir, float yDir) {
        // Change facing if appropriate

        if (xDir < 0) {
            faceRight = false;
        } else {
            faceRight = true;
        }
        applyForce(force, xDir, yDir);
    }

    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce(float force, float xDir, float yDir) {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (force == 0f) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache, getPosition(),true);
            return;
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            temp.set(xDir, yDir).nor();
            forceCache.set(force*temp.x*10,0f);
            body.applyForce(forceCache,getPosition(),true);
            forceCache.set(0,  force*temp.y*0.1f);
            body.applyLinearImpulse(forceCache,getPosition(),true);
        }

    }

    public void setThrownOut(boolean value){
        isThrownOut = value;
    }

    public boolean isThrowOut(){
        return isThrownOut;
    }

    public void setFaceRight(boolean faceRight) {
        this.faceRight = faceRight;
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return PENGUIN_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return PENGUIN_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return PENGUIN_MAXSPEED;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
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
    public Penguin(float x, float y, float width, float height, int index) {
        super(x,y,width* PLAYER_HSHRINK,height* PLAYER_VSHRINK);
        setDensity(PENGUIN_DENSITY);
        setFriction(PENGUIN_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        // Gameplay attributes
        isGrounded = false;
        faceRight = true;
        this.index = index;
        setName("penguin"+index);
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
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = PENGUIN_DENSITY;
        sensorDef.isSensor = true;
        sensorDef.filter.groupIndex = -1;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(PLAYER_SSHRINK *getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    public void setWalkingStrip(FilmStrip strip){
        this.walkingStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    public void setFilmStrip(FilmStrip strip){
        this.filmStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    public void setRolllingFilmStrip(FilmStrip strip){
        this.rollingStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    public void setIndex(int value){
        this.index = value;
    }

    public float getAngle(){
        return angle;
    }

    public int getIndex(){
        return index;
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
        if(timeCounter >= 0.2 && updateWalking && !isThrownOut) {
            timeCounter = 0;
            filmStrip.nextFrame();
        }else if(isThrownOut && !isGrounded && timeCounter >= 0.05 ){
            timeCounter = 0;
            float temp =velocityCache.set(getVX(), getVY()).len();
            temp = temp/25f*0.4f;
            angle += getVX() > 0 ? -temp : temp;
            angle %= Math.PI;
        }
        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        if(index != 0 && !isThrowOut()){
            return;
        }
        float effect = faceRight ? 1.0f : -1.0f;
        if(isThrownOut){
            canvas.draw(filmStrip, Color.WHITE, filmStrip.getRegionWidth()/2f, filmStrip.getRegionHeight()/2f, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1f, 1f);
        }else{
            canvas.draw(filmStrip,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,0,effect,1.0f);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}