package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Class for reading player input.
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.0f;
    private static final float GP_MAX_SPEED  = 10.0f;
    private static final float GP_THRESHOLD  = 0.01f;

    /** The singleton instance of the input controller */
    private static InputController theController = null;

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    // Fields to manage buttons
    /** Whether the A or left arrow button was pressed. */
    private boolean leftPressed;
    private boolean leftPrevious;
    /** Whether the D or right arrow button was pressed. */
    private boolean rightPressed;
    private boolean rightPrevious;
    /** Whether the button to advanced worlds was pressed. */
    private boolean nextPressed;
    private boolean nextPrevious;
    /** Whether the button to step back worlds was pressed. */
    private boolean prevPressed;
    private boolean prevPrevious;
    /** Whether the button to interact with the world was pressed. */
    private boolean xPressed;
    private boolean xPrevious;
    /** Whether the primary action button was pressed. */
    private boolean primePressed;
    private boolean primePrevious;
    /** Whether the secondary action button was pressed. */
    private boolean secondPressed;
    private boolean secondPrevious;
    /** Whether the teritiary action button was pressed. */
    private boolean tertiaryPressed;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;

    /** How much are we moving horizontally? */
    private float horizontal;
    /** How much did we move horizontally? */
    private float prevHorizontal;
    /** The crosshair position (for raddoll) */
    private Vector2 crosshair;
    /** The crosshair cache (for using as a return value) */
    private Vector2 crosscache;

    /** mouse click in x direction*/
    private float clickX;
    /** mouse click in y direction*/
    private float clickY;
    /** whether mouse is touched */
    private boolean isTouching;
    /** whether mouse is touched */
    private boolean prevIsTouching;
    /** whether mouse is touched */
    private boolean touchUp;

    private boolean throwPengiun = false;
    private boolean levelEditor = false;
    private boolean spacePressed = false;
    private boolean ePressed = false;
    private boolean jump = false;
    private boolean punch = false;

    /**
     * Returns the amount of sideways movement.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the current position of the crosshairs on the screen.
     *
     * This value does not return the actual reference to the crosshairs position.
     * That way this method can be called multiple times without any fair that
     * the position has been corrupted.  However, it does return the same object
     * each time.  So if you modify the object, the object will be reset in a
     * subsequent call to this getter.
     *
     * @return the current position of the crosshairs on the screen.
     */
    public Vector2 getCrossHair() {
        return crosscache.set(crosshair);
    }

    /**
     * Returns true if the primary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the primary action button was pressed.
     */
    public boolean didPrimary() {
        return primePressed && !primePrevious;
    }

    /**
     * Returns true if the secondary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didSecondary() {
        return secondPressed;
    }

    /**
     * Returns true if the secondary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didXPressed() {
        return xPressed && !xPrevious;
//        return secondPressed && !secondPrevious;
    }

    /**
     * Returns true if the tertiary action button was pressed.
     *
     * This is a sustained button. It will returns true as long as the player
     * holds it down.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didTertiary() {
        return tertiaryPressed;
    }

    /**
     * Returns true if the player wants to go to the next level.
     *
     * @return true if the player wants to go to the next level.
     */
    public boolean didAdvance() {
        return nextPressed && !nextPrevious;
    }

    /**
     * Returns true if the player wants to go to the previous level.
     *
     * @return true if the player wants to go to the previous level.
     */
    public boolean didRetreat() {
        return prevPressed && !prevPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
        return debugPressed && !debugPrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

    /**
     * Returns whether the pengiun is thrown
     *
     * @return whether the pengiun is thrown
     */
    public boolean didPressSpace() {
        return throwPengiun;
    }

    /**
     * Returns whether the pengiun is thrown
     *
     * @return whether the pengiun is thrown
     */
    public boolean didPressE() {
        return levelEditor;
    }

    /**
     * Returns whether the polar bear punches
     *
     * @return whether the polar bear punches
     */
    public boolean didPunch() {
        return punch;
    }

    /**
     *
     * @return mouse click in x direction
     */
    public float getClickX() {
        return clickX;
    }

    /**
     *
     * @return mouse click in x direction
     */
    public float getClickY() {
        return clickY;
    }

    /**
     *
     * @return mouse click
     */
    public boolean isTouching() {
        return isTouching;
    }

    /**
     *
     * @return mouse click up
     */
    public boolean touchUp() {
        return touchUp;
    }

    /**
     * Creates a new input controller
     *
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
        crosshair = new Vector2();
        crosscache = new Vector2();
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     *
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    public void readInput(Rectangle bounds, Vector2 scale) {
        // Copy state from last animation frame
        // Helps us ignore buttocrosshairns that are held down
        primePrevious  = primePressed;
        secondPrevious = secondPressed;
        debugPrevious  = debugPressed;
        exitPrevious = exitPressed;
        nextPrevious = nextPressed;
        prevPrevious = prevPressed;
        xPrevious = xPressed;
        leftPrevious = leftPressed;
        rightPrevious = rightPressed;

        // Check to see if a GamePad is connected
        readKeyboard(bounds, scale, false);
    }

    /**
     * Reads the input for this player and converts the result into game logic.
     *
     * This is an example of polling input.  Instead of registering a listener,
     * we ask the controller about its current state.  When the game is running,
     * it is typically best to poll input instead of using listeners.  Listeners
     * are more appropriate for menus and buttons (like the loading screen).
     */
    public void readInput() {
        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE) && spacePressed) {
            throwPengiun = true;
            spacePressed = false;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            spacePressed = true;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.E) && ePressed) {
            levelEditor = true;
            ePressed = false;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            ePressed = true;
        }
        prevIsTouching = isTouching;
        isTouching = Gdx.input.isTouched();
        if(prevIsTouching && !isTouching){
            touchUp = true;
        }else{
            touchUp = false;
        }
        clickX = Gdx.input.getX();
        clickY = Gdx.input.getY();
    }

    public boolean didTouchUp(){
        return touchUp;
    }
    public boolean getPrevIsTouching(){
        return prevIsTouching;
    }


    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
        // Give priority to gamepad results
        debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.Q));
        primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP)) || (Gdx.input.isKeyPressed(Input.Keys.W));
        secondPressed = (secondary && secondPressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
        prevPressed = (secondary && prevPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));
        nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.N));
        xPressed = (secondary && xPressed) || (Gdx.input.isKeyPressed(Input.Keys.Z));
        exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));

        clickX = Gdx.input.getX();
        clickY = Gdx.input.getY();

        prevIsTouching = isTouching;
        isTouching = Gdx.input.isTouched();
        if(prevIsTouching && !isTouching){
            touchUp = true;
        }else{
            touchUp = false;
        }

        // Directional controls
        prevHorizontal = horizontal;
        horizontal = 0;
        leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        if (rightPressed && !rightPrevious) {
            horizontal = 3.0f;
        } else if (leftPressed && !leftPrevious) {
            horizontal = -3.0f;
        } else if (rightPressed && prevHorizontal > 0) {
            horizontal = 1.0f;
        } else if (leftPressed && prevHorizontal < 0) {
            horizontal = -1.0f;
        }

        // Shooting
        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE) && spacePressed) {
            throwPengiun = true;
            spacePressed = false;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            spacePressed = true;
        }

        // Punching
        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            punch = true;
        } else {
            punch = false;
        }

        // Mouse results
        tertiaryPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        crosshair.set(Gdx.input.getX(), Gdx.input.getY());
        crosshair.scl(1/scale.x,-1/scale.y);
        crosshair.y += bounds.height;
        clampPosition(bounds);
    }

    /**
     * Clamp the cursor position so that it does not go outside the window
     *
     * While this is not usually a problem with mouse control, this is critical
     * for the gamepad controls.
     */
    private void clampPosition(Rectangle bounds) {
        crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
        crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
    }
}
