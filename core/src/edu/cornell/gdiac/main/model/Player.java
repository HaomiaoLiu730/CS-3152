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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.main.controller.InputController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.main.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.sql.Array;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Player extends CapsuleObstacle {
    private final JsonValue data;
    // Physics constants
    /** The density of the character */
    private final float PLAYER_DENSITY;
    /** The factor to multiply by the input */
    private final float PLAYER_FORCE;
    /** The amount to slow the character down */
    private final float PLAYER_DAMPING ;
    /** The dude is a slippery one */
    private final float PLAYER_FRICTION;
    /** The maximum character speed */
    private final float PLAYER_MAXSPEED;
    /** The impulse for the character jump */
    private final float PLAYER_JUMP;
    /** Cooldown (in animation frames) for jumping */
    private final int JUMP_COOLDOWN;
    /** Cooldown (in animation frames) for jumping */
    private final int THROW_COOLDOWN ;
    /** Cooldown (in animation frames) for shooting */
    private final int SHOOT_COOLDOWN ;
    /** Height of the sensor attached to the player's feet */
    private final float SENSOR_HEIGHT;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String SENSOR_NAME;
    /** max throwing force*/
    private final float MAX_THROWING_FORCE;

    private float PENGUIN_WIDTH;
    private float PENGUIN_HEIGHT;

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
    private FilmStrip penguinStrip;
    private FilmStrip penguinOverlapStrip;
    private FilmStrip normalStrip;

    /** The current horizontal movement of the character */
    private float movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Which direction is the character facing previously */
    private boolean prevFaceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** How long until we can throw penguin again*/
    private int throwCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** Whether we are actively jumping */
    private boolean isThrowing;
    /** is interrupted for throwing*/
    private boolean prevIsInterrupted;
    /** count for number of press */
    private int throwingCount;
    /** force for throwing */
    private float throwingForce;
    private boolean incr = true;
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
    private float timeCounter;
    private int totalPenguins;
    private int numPenguins;
    private boolean fixPenguin;
    public animationState moveState = animationState.walking;
    private float cameraX;
    private float[] trajectories = new float[10];

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
    /** Cache for internal direction calculations */
    private Vector2 directionCache = new Vector2();
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

    public void setNormalStrip(FilmStrip strip){
        normalStrip = strip;
    }

    public void setPenguinStrip(FilmStrip strip){
        this.penguinStrip=strip;
    }

    public void setPenguinOverlapStrip(FilmStrip strip){
        this.penguinOverlapStrip=strip;
    }

    public void setPenguinWalkingStrip(FilmStrip strip){
        this.penguinWalkingStrip = strip;
    }

    public void setPenguinRollingStrip(FilmStrip strip){
        this.penguinRollingStrip = strip;
    }

    public void setCameraX(float val){
        this.cameraX = val;
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
        prevFaceRight = faceRight;
        if (value < 0) {
            faceRight = false;
        } else if (value > 0) {
            faceRight = true;
        }

        if(faceRight != prevFaceRight){
            fixPenguin = true;
        }

        if(!isGrounded){
            for(Penguin p: penguins) {
                if(!p.isThrowOut()) p.setY(getY()-0.5f);
            }
        }

        if(fixPenguin){
            for(Penguin p: penguins){
                if(!p.isThrowOut()){
                    p.setSensor(true);
                    p.setBodyType(BodyDef.BodyType.StaticBody);
                    if(faceRight){
                        if(p.getX() - getX() < -PENGUIN_WIDTH){
                            fixPenguin = false;
                            p.setSensor(false);
//                            p.setBodyType(BodyDef.BodyType.DynamicBody);
                        }
                    }else{
                        if(p.getX() - getX() > PENGUIN_WIDTH){
                            fixPenguin = false;
                            p.setSensor(false);
//                            p.setBodyType(BodyDef.BodyType.DynamicBody);
                        }
                    }
                }
            }
        }else{
            for(Penguin p: penguins){
                if(!p.isThrowOut()){
                    p.setX(getX() + PENGUIN_WIDTH * (p.getIndex()+1) * (faceRight? -1 : 1));
                    p.setFaceRight(faceRight);
                }
            }
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
     * Set the number of penguins following the avatar.
     * @param i the number
     */
    public void setNumPenguins(int i){numPenguins = i; }



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
     */
    public void pickUpPenguins() {
            for(Penguin p: penguins){
                if(position.set(getPosition()).sub(p.getPosition()).len() < 1.5f && p.isThrowOut()){
                    p.setThrownOut(false);
                    p.setFilmStrip(penguinWalkingStrip);
                    p.setIndex(numPenguins);
                    p.setY(getY()-1);
                    p.setBodyType(BodyDef.BodyType.StaticBody);
                    numPenguins += 1;
                    if (numPenguins > 1) {
                        for (Penguin pen : penguins) {
                            pen.setOverlapFilmStrip(penguinOverlapStrip);
                        }
                    } else {
                        for (Penguin pen : penguins) {
                            pen.setOverlapFilmStrip(penguinStrip);
                        }
                    }
                }
            }
        }


    public void calculateTrajectory(float force, float xDir, float yDir){
        float dt =  0.01643628f;
        directionCache.set(xDir, yDir).nor();
        try{
            float vx = (float) (force*directionCache.x*10 * dt / Math.max(penguins.getFirst().getMass(), 1.3064942));
            float vy = (float) (force*directionCache.y*10f * dt / Math.max(penguins.getFirst().getMass(), 1.3064942));
            for(int i = 0; i<10; i+=2){
                float t = i * 0.05f;
                float x = ((getX() < 16 ? getX(): 16) + t * vx) * 1280 / 32f;
                float y = (getY()+2 + vy * t + 0.5f * (-26f) * t * t) * 720f/ 18f;
                trajectories[i] = x;
                trajectories[i+1] = y;
            }
        }catch (Exception e){
            return;
        }
    }
    public void setThrowing(boolean touchUp,Sound throwing){
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            throwingCount = -1;
        }
        if(touchUp && throwingCount == -1){
            throwingCount = 0;
            throwingForce = 0f;
            xDir = 0f;
            yDir = 0f;
        }else if(Gdx.input.isTouched() && throwingCount == 0){
            xDir = ((Gdx.input.getX() + cameraX-640))/1280f*32;
            yDir = (720-Gdx.input.getY())/720f*18;
            throwingForce += incr ? 5f : -5f;
            throwingForce = Math.min(throwingForce, MAX_THROWING_FORCE);
            if(throwingForce >= MAX_THROWING_FORCE){
                incr = false;
            }else if(throwingForce <= 0){
                incr = true;
            }
            calculateTrajectory(throwingForce, xDir-getX(), yDir-getY());
        }else if(touchUp && throwingCount == 0){
            if(numPenguins > 0){
                for(Penguin p: penguins){
                    if(p.getIndex() == numPenguins-1 && !p.isThrowOut()){
                        p.setBodyType(BodyDef.BodyType.DynamicBody);
                        p.setSensor(false);
                        setFilmStrip(throwingStrip);
                        throwing.play();
                        p.setFilmStrip(penguinRollingStrip);
                        p.setGrounded(false);
                        moveState = animationState.throwing;
                        p.setThrownOut(true);
                        p.setPosition(getX(), getY()+1.6f);
                        p.setActive(true);
                        p.setMovement(throwingForce, xDir-getX(), yDir-getY());
                        numPenguins -=1;
                        isThrowing = true;
                        throwingCount = 0;
                        throwingForce = 0f;
                        xDir = 0f;
                        yDir = 0f;
                        if (numPenguins==1){
                            for (Penguin pen: penguins) {
                                pen.setOverlapFilmStrip(penguinStrip);
                            }
                        }
                        return;
                    }
                }
            }
            if (numPenguins==1){
                for (Penguin pen: penguins) {
                    pen.setOverlapFilmStrip(penguinStrip);
                }
            }
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
     * @return how hard the brakes are applied to get a dude to stop moving
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
     * @param data      Data json
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Player(JsonValue data, JsonValue p_data, float width, float height, int numOfPenguins) {
        super(data.get("pos").getFloat(0),data.get("pos").getFloat(1),width* data.getFloat("hshrink"),height* data.getFloat("vshrink"));
        PLAYER_DENSITY=data.getFloat("density");
        PLAYER_FRICTION=data.getFloat("friction");
        PLAYER_FORCE=data.getFloat("force");
        PLAYER_DAMPING= data.getFloat("damping");
        PLAYER_MAXSPEED=data.getFloat("max_speed");
        PLAYER_JUMP=data.getFloat("player_jump");
        JUMP_COOLDOWN=data.getInt("jump_cooldown");
        THROW_COOLDOWN=data.getInt("throw_cooldown");
        SHOOT_COOLDOWN=data.getInt("shoot_cooldown");
        SENSOR_HEIGHT=data.getFloat("sensor_height");
        SENSOR_NAME=data.getString("sensor_name");
//        MAX_THROWING_FORCE=data.getFloat("max_throw_force");
        MAX_THROWING_FORCE = 440;
        PENGUIN_WIDTH=p_data.getFloat("width");
        PENGUIN_HEIGHT=p_data.getFloat("height");

        this.data=data;

        setDensity(PLAYER_DENSITY);
        setFriction(PLAYER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setRestitution(data.getFloat("restitution"));
        fixture.filter.groupIndex = -8;
        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
        isJumping = false;
        isPunching = false;
        faceRight = true;
        this.totalPenguins = numOfPenguins;
        this.numPenguins = totalPenguins;
        for(int i = 0; i < numOfPenguins; i++){
            penguins.add(new Penguin(p_data, p_data.getFloat("width"), p_data.getFloat("height"), i));
        }

        shootCooldown = data.getInt("shoot_cooldown");
        jumpCooldown = data.getInt("jump_cooldown");
        setName("dude");
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
        sensorShape.setAsBox(data.getFloat("sshrink") *getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
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
            setVX(0);
            if (moveState == animationState.walking && !isPunching) {
                setFilmStrip(normalStrip);
            }
        } else {
            setVX(Math.signum(getMovement())*getMaxSpeed());
            if (moveState == animationState.walking) {
                setFilmStrip(walkingStrip);
            }
        }

        // Jump!
        if (isJumping()) {
            setVY(PLAYER_JUMP);
        } else if (!isGrounded) {
            forceCache.set(0, -25f);
            body.applyForce(forceCache,getPosition(),true);
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
            if(timeCounter >= 0.1) {
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
            if(timeCounter >= 0.1) {
                timeCounter = 0;
                filmStrip.nextFrame();
                if (filmStrip.getFrame() == 0){
                    setFilmStrip(walkingStrip);
                    moveState = animationState.walking;
                }
            }
        }

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
            if(Math.abs(p.getY()-getY()) > 3f && !p.isThrowOut()){
                p.setY(getY());
            }
            p.applyForce(0,0, 0);
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
        if(Gdx.input.isTouched()&& throwingCount == 0 && numPenguins > 0){
            for(int i = 0; i<trajectories.length; i+=2){
                canvas.drawCircle(Color.BLACK,trajectories[i],trajectories[i+1], 4-i*0.1f);
                canvas.drawCircle(Color.WHITE,trajectories[i],trajectories[i+1], 2-i*0.1f);
            }
        }
        //canvas.draw();
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

    public void setMovingIceoffset(float x){
        setPosition(getX()-x,getY());
    }
}
