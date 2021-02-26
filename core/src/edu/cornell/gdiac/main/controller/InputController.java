package edu.cornell.gdiac.main.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

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
    private boolean moveLeft;
    private boolean moveRight;
    private boolean jump;

    private boolean pressed;

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
     * Returns the true if left
     *
     * @return whether move left
     */
    public boolean getMoveLeft() {
        return moveLeft;
    }

    /**
     * Returns true if right
     *
     * @return whether to move right
     */
    public boolean getMoveRight() {
        return moveRight;
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
            moveLeft = moveRight = jump = false;
            throwPengiun = false;

            // Movement forward/backward
            if (Gdx.input.isKeyPressed(left) && !Gdx.input.isKeyPressed(right)) {
                moveLeft = true;
            } else if (Gdx.input.isKeyPressed(right) && !Gdx.input.isKeyPressed(left)) {
                moveRight = true;
            }

            // Movement left/right
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
}