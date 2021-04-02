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

import edu.cornell.gdiac.main.obstacle.*;

public class Ice extends ComplexObstacle {
    /** The initializing data (to avoid magic numbers) */
    //private final JsonValue data;

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
     * @param x  	    The x coordinate of the center
     * @param y         The y coordinate of the center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Ice(float x,float y, float width, float height) {
        super(x,y);
        setName("Ice");

//         Create the barrier
        iceBar = new BoxObstacle(x,y,width,height);
        iceBar.setName("iceBar");
        iceBar.setDensity(3f);
        iceBar.setFriction(2f);
        iceBar.setRestitution(0);
        iceBar.setFixedRotation(true);
        bodies.add(iceBar);


        //#region INSERT CODE HERE
        // Create a pin to anchor the barrier 
        // Radius:  data.getFloat("radius")
        // Density: data.getFloat("low_density")
        // Name: "pin"
        pin = new WheelObstacle(x,y,0.1f);
        pin.setName("pin");
        pin.setDensity(0);
        pin.setBodyType(BodyDef.BodyType.StaticBody);
        pin.setRestitution(0);

        //pin.setActive(false);
        bodies.add(pin);
        //pin.activatePhysics(world);

        //#endregion
    }

    public void setTranform(float x, float y,float angle){
        super.getBody().setTransform(x,y,angle);
        for(Obstacle obj : bodies) {
            obj.getBody().setTransform(x,y,angle);
        }
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

        //#region INSERT CODE HERE
        // Attach the barrier to the pin here
        Vector2 anchorA = new Vector2();
        Vector2 anchorB = new Vector2();
        anchorB.y = 0.1f;
        // Definition for a revolute joint
        RevoluteJointDef jointDef = new RevoluteJointDef();

        // Initial joint
        jointDef.bodyB = iceBar.getBody();
        jointDef.bodyA = pin.getBody();
        jointDef.localAnchorB.set(anchorB);
        jointDef.localAnchorA.set(anchorA);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);
        //#endregion

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
