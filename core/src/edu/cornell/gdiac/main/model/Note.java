package edu.cornell.gdiac.main.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.main.obstacle.BoxObstacle;
import edu.cornell.gdiac.main.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

import static com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody;

public class Note extends BoxObstacle {
    // Physics constants

    // This is to fit the image to a tighter hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float NOTE_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float NOTE_HSHRINK = 0.8f;

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
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Note(float x, float y, float width, float height, int index) {
        super(x,y,width* NOTE_HSHRINK,height* NOTE_VSHRINK);
        setBodyType(StaticBody);
        setSensor(true);
        setName("note"+index);
        this.index = index;
        isCollected = false;
    }

    public void setCollected(boolean val){
        isCollected = val;
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
