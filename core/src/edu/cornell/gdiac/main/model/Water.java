package edu.cornell.gdiac.main.model;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.obstacle.BoxObstacle;
import edu.cornell.gdiac.main.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
public class Water extends BoxObstacle {
    private final JsonValue data;

    /** The density of the character */
    private final float WATER_DENSITY;
    /** The factor to multiply by the input */
    private final float WATER_FORCE;
    /** The dude is a slippery one */
    private final float WATER_FRICTION;
    private FilmStrip waterStrip;
    private FilmStrip wavesStrip;
    private float pos_x;
    private float pos_y;
    private float width;
    private float height;
    private int index;

    private Color transparent = new Color(255, 255, 255, 0.5f);


    private float timeCounter;

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
        return WATER_FORCE;
    }
    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data      Json Data
     * @param width     The object width in physics units
     * @param height    The object width in physics units
     */
    public Water(JsonValue data, float width, float height, String name,int index) {
        super(data.get("pos").get(index).getFloat(0), data.get("pos").get(index).getFloat(1),width,height);
        pos_x = data.get("pos").get(index).getFloat(0);
        pos_y = data.get("pos").get(index).getFloat(1);
        this.width=width;
        this.height=height;
        this.index=index;
        WATER_DENSITY=data.getFloat("density");
        WATER_FORCE=data.getFloat("force");
        WATER_FRICTION=data.getFloat("friction");
        this.data=data;
        setDensity(WATER_DENSITY);
        setFriction(WATER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setRestitution(data.getFloat("restitution"));
        setFixedRotation(true);
        setActive(false);
        setAwake(false);
        setName(name+index);
        setSensor(true);
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
    public void setFilmStrip(FilmStrip waterStrip, FilmStrip wavesStrip){
        this.waterStrip = waterStrip.copy();
        this.wavesStrip = wavesStrip;
        this.waterStrip.setRegionWidth((int)width*40);
        this.waterStrip.setRegionHeight((int)(height-1)*40);
        origin.set(this.waterStrip.getRegionWidth()/2.0f, this.waterStrip.getRegionHeight()/2.0f);
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
        if (index == 0) {
            this.timeCounter += dt;
            if (this.timeCounter >= 0.175) {
                this.timeCounter = 0;
                this.wavesStrip.nextFrame();
            }
            super.update(dt);
        }
    }
    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float startX = (getX()-width/2f)*drawScale.x;
        float y = (getY()+height/2f-1f)*drawScale.y;
        for (int i = 0; i<width; i++) {
            float x = startX+40*i;
            canvas.draw(wavesStrip,transparent,x, y, wavesStrip.getRegionWidth(), wavesStrip.getRegionHeight());
        }
        canvas.draw(waterStrip,transparent,getX()*drawScale.x-waterStrip.getRegionWidth()/2f, (getY()-0.5f)*drawScale.y- waterStrip.getRegionHeight()/2f, waterStrip.getRegionWidth(), waterStrip.getRegionHeight());
    }
}