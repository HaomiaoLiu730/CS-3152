package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;


public class Ice extends PolygonObstacle {
    // Physics constants
    /**
     * The density of the character
     */
    private static final float DENSITY = 0.9f;
    /**
     * The dude is a slippery one
     */
    private static final float FRICTION = 5.0f;

    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private static final String SENSOR_NAME = "WaterSensor";


    /**
     * Whether our feet are on the ground
     */
    private boolean isGrounded;
    /**
     * Ground sensor to represent our feet
     */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;

    private float[] vertices;

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
     * Returns the name of the ground sensor
     * <p>
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }


    /**
     * Creates a new dude avatar at the given position.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x Initial x position of the avatar center
     * @param y Initial y position of the avatar center
     */
    public Ice(float[] points, float x, float y, String name) {
        super(points, x, y);
        setDensity(DENSITY);
        setFriction(FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setRestitution(0.1f);
        setFixedRotation(true);

        // Gameplay attributes
        isGrounded = false;

        setName(name);
        vertices = points;
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.set(vertices);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }
}