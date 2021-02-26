package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Device-independent input manager.
 *
 * This class supports both a keyboard and an X-Box controller.  Each player is
 * assigned an ID.  When the class is created, we check to see if there is a
 * controller for that ID.  If so, we use the controller.  Otherwise, we default
 * the the keyboard.
 */
public class InputController {

    /** polar bear moving left / right */
    private boolean jump;

    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;

    // Fields to manage buttons
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    private boolean pressed;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;
    /** Whether the primary action button was pressed. */
    private boolean primePressed;
    private boolean primePrevious;
    /** Whether the secondary action button was pressed. */
    private boolean secondPressed;
    private boolean secondPrevious;
    /** Whether the teritiary action button was pressed. */
    private boolean tertiaryPressed;

    /** throw pengiun */
    private boolean throwPengiun;

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
     * Returns the amount of vertical movement.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }


    /**
     * Returns true if jump
     *
     * @return whether to jump
     */
    public boolean didJump() {
        return jump;
    }

    /**
     * Returns whether the pengiun is thrown
     *
     * @return whether the pengiun is thrown
     */
    public boolean didThrowPengiun() {
        return throwPengiun;
    }

    /**
     * Creates a new input controller for the player.
     *
     */
    public InputController() {

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
        int up, left, right, down, spacePressed;
        up = Input.Keys.UP;
        down = Input.Keys.DOWN;
        left = Input.Keys.LEFT;
        right = Input.Keys.RIGHT;
        spacePressed = Input.Keys.SPACE;

        // Convert keyboard state into game commands
        jump = false;
        throwPengiun = false;

        // Directional controls
        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }

        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

        if (Gdx.input.isKeyPressed(up) && !Gdx.input.isKeyPressed(down)) {
            jump = true;
        }

        // Shooting
        if (!Gdx.input.isKeyPressed(spacePressed) && pressed) {
            throwPengiun = true;
            pressed = false;
        }
        if(Gdx.input.isKeyPressed(spacePressed)){
            pressed = true;
        }
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
        // Helps us ignore buttons that are held down
        primePrevious  = primePressed;
        secondPrevious = secondPressed;
        resetPrevious  = resetPressed;
        debugPrevious  = debugPressed;
        exitPrevious = exitPressed;
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
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

}