package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.main.controller.WorldController;

public class GameMode extends WorldController implements ModeController {

    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    // Physics constants for initialization
    /** Density of non-crate objects */
    private static final float BASIC_DENSITY   = 0.0f;
    /** Density of the crate objects */
    private static final float CRATE_DENSITY   = 1.0f;
    /** Friction of non-crate objects */
    private static final float BASIC_FRICTION  = 0.1f;
    /** Friction of the crate objects */
    private static final float CRATE_FRICTION  = 0.3f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0.1f;
    /** Threshold for generating sound on collision */
    private static final float SOUND_THRESHOLD = 1.0f;

    /** The world scale */
    protected Vector2 scale;


    // Wall vertices
    private static final float[][] WALLS = {
            {16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
                    1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
            {32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
                    31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f}
    };

    /** The outlines of all of the platforms */
    private static final float[][] PLATFORMS = {
            { 1.0f, 3.0f, 6.0f, 3.0f, 6.0f, 2.5f, 1.0f, 2.5f},
            { 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
            {23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
            {26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
            {29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
            {24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
            {29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
            {23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
            {19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
            { 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f}
    };


    /**
     * Creates a new game with a playing field of the given size.
     * <p>
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     *
     * @param width  The width of the game window
     * @param height The height of the game window
     * @param assets The asset directory containing all the loaded assets
     */
    public GameMode(float width, float height, AssetDirectory assets) {
        goalTile = assets.getEntry("goalTile", TextureRegion.class);

    }

    @Override
    public void update() {

    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth = goalTile.getRegionWidth() / scale.x;
        float dheight = goalTile.getRegionHeight() / scale.y;

        String wname = "wall";
        for (int ii = 0; ii < WALLS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(WALLS[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(wname+ii);
            addObject(obj);
        }

        String pname = "platform";
        for (int ii = 0; ii < PLATFORMS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(PLATFORMS[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(pname+ii);
            addObject(obj);
        }

    }

        @Override
    public void draw(GameCanvas canvas) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void resize(int width, int height) {

    }
}
