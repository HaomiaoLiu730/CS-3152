package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

public class Monster extends CapsuleObstacle {
    private final JsonValue data;

    // Physics constants
    /** The density of the monster */
    private float MONSTER_DENSITY;
    /** The factor to multiply by the input */
    private float MONSTER_FORCE;
    /** The amount to slow the character down */
    private float MONSTER_DAMPING;
    /** The monster is a slippery one */
    private float MONSTER_FRICTION;

    /** The maximum character speed */
    private final float MONSTER_MAXSPEED;
    /** Height of the sensor attached to the monster's feet */
    private final float SENSOR_HEIGHT;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String SENSOR_NAME;


    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** The range of movements */
    private float moveRange;
    /** Which direction is the character facing */
    private int faceRight;

    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private FilmStrip filmStrip;
    private float timeCounter = 0;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public int isFacingRight() {
        return faceRight;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setFacingRight(int value) {
        faceRight = value;
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
        return MONSTER_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return MONSTER_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return MONSTER_MAXSPEED;
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
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data       Json Data
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Monster(JsonValue data, float width, float height, String name, float range, int index) {
        super(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1),width*data.getFloat("hshrink"),height*data.getFloat("vshrink"));
        MONSTER_DENSITY= data.getFloat("density");
        MONSTER_FORCE=data.getFloat("force");
        MONSTER_DAMPING=data.getFloat("damping");
        MONSTER_FRICTION=data.getFloat("friction");
        MONSTER_MAXSPEED=data.getFloat("maxspeed");
        SENSOR_HEIGHT=data.getFloat("sensorheight");
        SENSOR_NAME=data.getString("sensorname");
        this.data=data;
        setDensity(MONSTER_DENSITY);
        setFriction(MONSTER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setRestitution(data.getFloat("restitution"));
        setFixedRotation(true);

        // Gameplay attributes
        isGrounded = false;
        faceRight = -1;
        moveRange = range;
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

        // Ground Sensor
        // -------------
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = MONSTER_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(data.getFloat("sshrink")*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }

    /** Set the animation of the monster */
    public void setFilmStrip(FilmStrip strip){
        this.filmStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    /** Applies the force to the body of the monster */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (Math.abs(forceCache.x) >= Math.abs(faceRight*moveRange)) {
            faceRight = -1*faceRight;
            forceCache.set(faceRight*10,0);
        } else {
            float formerX = forceCache.x;
            forceCache.set(formerX+faceRight*1f,0);
        }
        body.applyForce(forceCache,getPosition(),true);
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
        float dir = faceRight == 1 ? -1.0f : 1.0f;
        canvas.draw(filmStrip,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),dir*0.25f, 0.25f);
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
