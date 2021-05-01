package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.obstacle.BoxObstacle;
import edu.cornell.gdiac.main.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

import static com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody;

public class Note extends BoxObstacle {
    private final JsonValue data;
    // Physics constants

    // This is to fit the image to a tighter hitbox

    private FilmStrip filmStrip;
    private boolean isCollected;
    private int index;

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data      Json data
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Note(JsonValue data, float width, float height, int index) {
        super(data.get("pos").get(index).getFloat(0), data.get("pos").get(index).getFloat(1),width* data.getFloat("hshrink"),height* data.getFloat("vshrink"));
        setBodyType(StaticBody);
        setSensor(true);
        setName("note"+index);
        this.index = index;
        isCollected = false;
        this.data=data;
    }

    public void setCollected(boolean val){
        isCollected = val;
        setActive(false);
        setAwake(false);
    }

    public boolean isCollected(){
        return isCollected;
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
