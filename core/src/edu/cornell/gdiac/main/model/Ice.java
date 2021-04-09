/*
 * Spinner.java
 *
 * This class provides a spinning rectangle on a fixed pin.  We did not really need
 * a separate class for this, as it has no update.  However, ComplexObstacles always
 * make joint management easier.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.obstacle.*;

public class Ice extends ComplexObstacle {
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    /** The primary spinner obstacle */
    private BoxObstacle iceBar;
    private WheelObstacle pin;

    /**
     * Creates a new spinner with the given physics data.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data        Json Data
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Ice(JsonValue data, int index, float width, float height) {
        super(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1));
        setName("Ice");

        iceBar = new BoxObstacle(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1),width,height);
        iceBar.setName("iceBar");
        iceBar.setDensity(data.getFloat("bar_density"));
        iceBar.setFriction(data.getFloat("friction"));
        iceBar.setRestitution(data.getFloat("restitution"));
        iceBar.setFixedRotation(true);
        iceBar.setAngularDamping(0.5f);
        bodies.add(iceBar);


        pin = new WheelObstacle(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1),data.getFloat("pin_radius"));
        pin.setName("pin");
        pin.setDensity(data.getFloat("pin_density"));
        pin.setBodyType(BodyDef.BodyType.StaticBody);
        pin.setRestitution(data.getFloat("restitution"));

        bodies.add(pin);
        this.data=data;
    }


    public float getX(){
        return iceBar.getPosition().x;
    }

    /**
     * Creates the joints for this object.
     *
     * We implement our custom logic here.
     *
     * @param world Box2D world to store joints
     *
     * @return true if object allocation succeeded
     */
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        Vector2 anchorA = new Vector2();
        Vector2 anchorB = new Vector2(0,iceBar.getHeight()*3/8);
        RevoluteJointDef jointDef = new RevoluteJointDef();

        jointDef.bodyB = iceBar.getBody();
        jointDef.bodyA = pin.getBody();
        jointDef.localAnchorB.set(anchorB);
        jointDef.localAnchorA.set(anchorA);
        jointDef.collideConnected = false;
        jointDef.lowerAngle = -0.08f * (float)Math.PI;
        jointDef.upperAngle = 0.08f * (float)Math.PI;
        jointDef.enableLimit = true;

        Joint joint = world.createJoint(jointDef);
        joints.add(joint);

        return true;
    }

    public void setTexture(TextureRegion texture) {
        iceBar.setTexture(texture);
    }

    public TextureRegion getTexture() {
        return iceBar.getTexture();
    }

    public void setTilting(boolean tilt){
        iceBar.setFixedRotation(!tilt);
    }
}
