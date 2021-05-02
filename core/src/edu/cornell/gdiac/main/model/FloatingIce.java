package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.obstacle.BoxObstacle;
import edu.cornell.gdiac.main.obstacle.ComplexObstacle;
import edu.cornell.gdiac.main.obstacle.WheelObstacle;

public class FloatingIce extends ComplexObstacle {
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    /** The primary spinner obstacle */
    private BoxObstacle iceBar;
    private WheelObstacle pin;
    private float momentum;
    private int index;
    private int coolDownCount;
    private boolean offsetFlag = false;

    private int direction;

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
    public FloatingIce(JsonValue data, int index, float width, float height) {
        super(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1));
        setName("floatingIce");

        coolDownCount = 0;

        this.index = index;

        iceBar = new BoxObstacle(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1),width,height);
        iceBar.setName("floatingIceBar");
        iceBar.setDensity(data.getFloat("bar_density"));
        iceBar.setFriction(data.getFloat("friction"));
        iceBar.setRestitution(data.getFloat("restitution"));
        iceBar.setFixedRotation(true);
        iceBar.setAngularDamping(0.5f);
        iceBar.setMaster(this);
        bodies.add(iceBar);


        pin = new WheelObstacle(data.get("pos").get(index).getFloat(0),data.get("pos").get(index).getFloat(1),data.getFloat("pin_radius"));
        pin.setName("pin");
        pin.setDensity(data.getFloat("pin_density"));
        pin.setBodyType(BodyDef.BodyType.StaticBody);
        pin.setRestitution(data.getFloat("restitution"));

        bodies.add(pin);
        this.data=data;

        direction = 0;
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

    @Override
    public void update(float delta) {
        if (isPaused()) return;
        super.update(delta);
        if(momentum!=0) {
            pin.setPosition(pin.getX() - momentum, pin.getY());
            if(momentum >0)
                momentum -= 0.0009;
            else
                momentum += 0.0009;
            if (Math.abs(momentum) < 0.001) {
                momentum = 0;
            }
        }
        if (offsetFlag) {
            float offset = direction * 0.03f;
            if(momentum != 0)
                offset = -momentum *5;
            pin.setPosition(pin.getX() - offset, pin.getY());
            momentum = 0;
            offsetFlag = false;
        }
    }

    public void hitByIcicle(float force){
        if(momentum == 0)
            momentum = force;
        if(momentum>0) direction = 1;
        else direction = -1;
    }

    public void resetMomentum(){
        momentum = 0;
    }

    public void offsetX(){
        offsetFlag = true;
    }
}

