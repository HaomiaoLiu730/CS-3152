package edu.cornell.gdiac.main.model;

/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Player extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float PLAYER_DENSITY = 1.0f;
    /** The factor to multiply by the input */
    private static final float PLAYER_FORCE = 12.0f;
    /** The amount to slow the character down */
    private static final float PLAYER_DAMPING = 10.0f;
    /** The dude is a slippery one */
    private static final float PLAYER_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float PLAYER_MAXSPEED = 3.0f;
    /** The impulse for the character jump */
    private static final float PLAYER_JUMP = 16f;
    /** Cooldown (in animation frames) for jumping */
    private static final int JUMP_COOLDOWN = 30;
    /** Cooldown (in animation frames) for jumping */
    private static final int THROW_COOLDOWN = 30;
    /** Cooldown (in animation frames) for shooting */
    private static final int SHOOT_COOLDOWN = 40;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "DudeGroundSensor";
    /** max throwing force*/
    private static final float MAX_THROWING_FORCE = 200;

    private float PENGUIN_WIDTH = 1.6f;
    private float PENGUIN_HEIGHT = 2f;

    /** The texture for the player jumping */
    private FilmStrip jumpRisingStrip;
    /** The texture for the player jumping */
    private FilmStrip jumpHangingStrip;
    /** The texture for the player jumping */
    private FilmStrip jumpLandingStrip;
    /** The texture for the player walking */
    private FilmStrip walkingStrip;
    /** The texture for the player throwing */
    private FilmStrip throwingStrip;
    private FilmStrip penguinWalkingStrip;
    private FilmStrip penguinRollingStrip;

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float PLAYER_VSHRINK = 0.25f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float PLAYER_HSHRINK = 0.25f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float PLAYER_SSHRINK = 0.6f;

    /** The current horizontal movement of the character */
    private float movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** How long until we can throw penguin again*/
    private int throwCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** Whether we are actively interacting */
    private boolean isInteracting;
    /** Whether we are actively jumping */
    private boolean prevIsThrowing;
    /** Whether we are actively jumping */
    private boolean isThrowing;
    /** count for number of press */
    private int throwingCount;
    /** force for throwing */
    private float throwingForce;
    /** angle for throwing */
    private float xDir;
    private float yDir;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** Whether we are actively shooting */
    private boolean isPunching;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private FilmStrip filmStrip;
    private Texture arrowTexture;
    private Texture energyBarOutline;
    private Texture energyBar;
    private float timeCounter = 0;
    private int totalPenguins;
    private int numPenguins;
    public animationState moveState = animationState.walking;

    public enum animationState {
        /** walking state*/
        walking,
        /** rising for jumping*/
        jumpRising,
        /** handing for jumping */
        jumpHanging,
        /** landing for jumping */
        jumpLanding,
        /** throwing penguins*/
        throwing,
    }

    private LinkedList<Penguin> penguins = new LinkedList<>();

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();
    /** Cache for internal position calculations */
    private Vector2 position = new Vector2();

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    public void setJumpRisingStrip(FilmStrip strip){
        jumpRisingStrip = strip;
    }

    public void setJumpHangingStrip(FilmStrip strip){
        jumpHangingStrip = strip;
    }

    public void setJumpLandingStrip(FilmStrip strip){
        jumpLandingStrip = strip;
    }

    public void setWalkingStrip(FilmStrip strip){
        walkingStrip = strip;
    }

    public void setThrowingStrip(FilmStrip strip){
        throwingStrip = strip;
    }

    public void setPenguinWalkingStrip(FilmStrip strip){
        this.penguinWalkingStrip = strip;
    }

    public void setPenguinRollingStrip(FilmStrip strip){
        this.penguinRollingStrip = strip;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (value < 0) {
            faceRight = false;
        } else if (value > 0) {
            faceRight = true;
        }

        for(int i = 0; i<penguins.size(); i++){
            if(!penguins.get(i).isThrowOut()){
                penguins.get(i).setX(getX() + PENGUIN_WIDTH * (1) * (faceRight? -1 : 1));
                penguins.get(i).setY(getY());
                penguins.get(i).setFaceRight(faceRight);
            }
//            if(!p.isThrowOut() || !p.isGrounded()){
//                p.setY(getY());
//            }
        }
    }

    /**
     * get all penguins
     * @return all penguins
     */
    public LinkedList<Penguin> getPenguins(){
        return penguins;
    }

    /**
     * Returns the number of penguins following the avatar.
     *
     */
    public int getNumPenguins(){return numPenguins; }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }

    /**
     * Returns true if the dude is actively jumping.
     *
     * @return true if the dude is actively jumping.
     */
    public boolean isJumping() {
        return isJumping && isGrounded && jumpCooldown <= 0;
    }

    /**
     * Returns true if the dude is actively throwing penguin.
     *
     * @return true if the dude is actively throwing penguin.
     */
    public boolean isThrowing() {
        return isThrowing && isGrounded && throwCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively jumping.
     *
     * @param value whether the dude is actively jumping.
     */
    public void setJumping(boolean value) {
        isJumping = value;
    }

    /**
     * Sets whether the dude is actively jumping.
     *
     * @param value whether the dude is actively jumping.
     */
    public void setInteract(boolean value) {
        isInteracting = value;
        if(isInteracting){
            for(Penguin p: penguins){
                if(position.set(getPosition()).sub(p.getPosition()).len() < 2 && p.isThrowOut()){
                    p.getBody().setType(BodyDef.BodyType.StaticBody);
                    p.setThrownOut(false);
                    p.setFilmStrip(penguinWalkingStrip);
                    p.setIndex(numPenguins);
                    numPenguins += 1;
                }
            }
        }
    }

    public void setThrowing(float clickX, float clickY, float avatarX, float avatarY, boolean touchUp, boolean isTouching ) {
        isThrowing = isTouching;
        // setting throwing direction
        if(touchUp && throwingCount == 0){
            xDir = clickX/1280f*32 - avatarX;
            yDir = (720f-clickY)/720f*18 - avatarY;
            throwingCount = 1;
        }else if(throwingCount == 1 && isTouching){
            // setting force
            throwingForce += 5f;
            throwingForce = Math.min(throwingForce, MAX_THROWING_FORCE);
        }else if(throwingCount == 1 && !isTouching && throwingForce != 0f){
            if(numPenguins > 0){
                for(Penguin p: penguins){
                    if(p.getIndex() == numPenguins-1){
                        p.getBody().setType(BodyDef.BodyType.DynamicBody);
                        setFilmStrip(throwingStrip);
                        p.setFilmStrip(penguinRollingStrip);
                        moveState = animationState.throwing;
                        p.setThrownOut(true);
                        p.setPosition(getX(), getY()+2);
                        p.setMovement(throwingForce, xDir, yDir);
                        numPenguins -=1;
                        break;
                    }
                }
            }
            throwingCount = 0;
            throwingForce = 0f;
            xDir = 0f;
            yDir = 0f;
        }

    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isPunching() {
        return isPunching;
    }

    /**
     * Sets whether the polar bear is punching.
     *
     * @param value whether the polar bear is punching.
     */
    public void setPunching(boolean value) {
        isPunching = value;
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return PLAYER_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return PLAYER_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return PLAYER_MAXSPEED;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

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
    public Player(float x, float y, float width, float height, int numOfPenguins) {
        super(x,y,width* PLAYER_HSHRINK,height* PLAYER_VSHRINK);
        setDensity(PLAYER_DENSITY);
        setFriction(PLAYER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setRestitution(0f);
        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
        isJumping = false;
        isPunching = false;
        faceRight = true;
        this.totalPenguins = numOfPenguins;
        this.numPenguins = totalPenguins;
        for(int i = 0; i < numOfPenguins; i++){
            penguins.add(new Penguin(x - (i+1)*PENGUIN_WIDTH, y,PENGUIN_WIDTH, PENGUIN_HEIGHT, i));
        }

        shootCooldown = 0;
        jumpCooldown = 0;
        setName("dude");
    }

    public void setPenguinWidth(float width){
        this.PENGUIN_WIDTH = width;
    }

    public void setPenguinHeight(float height){
        this.PENGUIN_HEIGHT = height;
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

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = PLAYER_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(PLAYER_SSHRINK *getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            forceCache.set(getMovement(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Jump!

        if (isJumping()) {
            forceCache.set(0, PLAYER_JUMP);
            body.applyLinearImpulse(forceCache,getPosition(),true);
        }
    }

    public void setFilmStrip(FilmStrip strip){
        this.filmStrip = strip;
        origin.set(strip.getRegionWidth()/2.0f, strip.getRegionHeight()/2.0f);
    }

    public void setArrowTexture(Texture arrow){
        this.arrowTexture = arrow;
    }
    public void setEnergyBarOutline(Texture texture){
        this.energyBarOutline = texture;
    }
    public void setEnergyBar(Texture texture){
        this.energyBar = texture;
    }

    public float vToDelta(float v){
        float k = (0.3f - 0.05f) / (0.1f - PLAYER_MAXSPEED);
        float b = 0.05f - PLAYER_MAXSPEED*k;
        return k*v+b;
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
        timeCounter += dt;

        if(moveState == animationState.walking && Math.abs(getVX())>0.01f){
            if(timeCounter >= vToDelta(Math.abs(getVX())) && Math.abs(getVX())>0.1f) {
                timeCounter = 0;
                filmStrip.nextFrame();
            }
        }else if(moveState == animationState.jumpRising){
            if(timeCounter >= 0.2) {
                timeCounter = 0;
                filmStrip.nextFrame();
                if (filmStrip.getFrame() == 0){
                    moveState = animationState.jumpHanging;
                    setFilmStrip(jumpHangingStrip);
                }
            }
        }else if(moveState == animationState.jumpHanging){
            // nothing here
        }else if(moveState == animationState.jumpLanding || moveState == animationState.throwing){
            if(timeCounter >= 0.2) {
                timeCounter = 0;
                filmStrip.nextFrame();
                if (filmStrip.getFrame() == 0){
                    setFilmStrip(walkingStrip);
                    moveState = animationState.walking;
                }
            }
        }


//        if(isGrounded){
//            if(timeCounter >= 0.1 && Math.abs(getVX()) > 1e-1) {
//                timeCounter = 0;
//                filmStrip.nextFrame();
//            }
//        }else{
//            if(timeCounter >= 0.2 && Math.abs(getVX()) > 1e-1) {
//                timeCounter = 0;
//                filmStrip.nextFrame();
//            }
//        }
        if (isJumping()) {
            jumpCooldown = JUMP_COOLDOWN;
        } else {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }
        if (isThrowing()) {
            throwCooldown = THROW_COOLDOWN;
        } else {
            throwCooldown = Math.max(0, throwCooldown - 1);
        }
        if (isShooting()) {
            shootCooldown = SHOOT_COOLDOWN;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }


        for(Penguin p: penguins){
            p.updateWalking = (Math.abs(getVX()) >= 0.1f)? true: false;
            p.applyForce(0,0, 0);
//            p.update(dt);
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        float throwingAngle = (float)(Math.atan(yDir/xDir));
        if(throwingAngle < 0){
            throwingAngle = -throwingAngle + (float)(Math.PI/2);
        }
        if(throwingAngle != 0){
            canvas.draw(arrowTexture, Color.BLACK, arrowTexture.getWidth()/2f, arrowTexture.getHeight()/2f, getX()*drawScale.x, getY()*drawScale.y+40, throwingAngle, 1f, 1f);
        }
        if(throwingCount == 1  && isThrowing){
            canvas.draw(energyBar, Color.WHITE, energyBar.getWidth()/2f, 0, getX()*drawScale.x-30, getY()*drawScale.y, 0,1f, throwingForce/MAX_THROWING_FORCE);
            canvas.draw(energyBarOutline, getX()*drawScale.x-40, getY()*drawScale.y);
        }
        canvas.draw(filmStrip,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*0.25f,0.25f);
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}