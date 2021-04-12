package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.Obstacle;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.List;

public class CollisionController {

    private float width;
    private float height;

    private float distMonsterAvatar;

    /**
     * Creates a CollisionController for the given screen dimensions.
     *
     * @param width  Width of the screen
     * @param height Height of the screen
     */
    public CollisionController(float width, float height) {
        this.width = width;
        this.height = height;

        // Initialize cache objects
    }


    public void processCollision(ArrayList<Monster> monsters, Player avatar, PooledList<Obstacle> objects ){
        // Monster moving and attacking
        for (int i = 0; i < monsters.size(); i++) {
            if (monsters.get(i).isActive()) {
                distMonsterAvatar = avatar.getPosition().dst(monsters.get(i).getPosition());
                if (avatar.isPunching()) {
                    if (distMonsterAvatar < 3) {
                        monsters.get(i).setActive(false);
                        monsters.get(i).setAwake(false);
                        objects.remove(monsters.get(i));
                    }
                }
            }
        }
    }

    public void processCollision(ArrayList<Monster> monsters, FilmStrip attackStrip, List<Penguin> penguins){
        for (int i = 0; i < monsters.size(); i++) {
            if (monsters.get(i).isActive()) {
                boolean moveMon = true;
                for(Penguin p: penguins){
                    float dist2 = p.getPosition().dst(monsters.get(i).getPosition());
                    if (dist2 < 3 && dist2 < distMonsterAvatar) {
                        monsters.get(i).setFilmStrip(attackStrip);
                        if (p.getPosition().x < monsters.get(i).getPosition().x) {
                            monsters.get(i).setFacingRight(-1);
                        }
                        moveMon = false;
                        GameplayController.resetCountDown -= 1;
                    }
                }
                if (moveMon) {
                    monsters.get(i).applyForce();
                }
            }
        }
    }

    public void processCollision(ArrayList<Monster> monsters, PolygonObstacle icicle, PooledList<Obstacle> objects){
        for (int i = 0; i < monsters.size(); i++) {
            if (monsters.get(i).isActive()) {
                if (icicle.getPosition().dst(monsters.get(i).getPosition()) <= 1){
                    objects.remove(monsters.get(i));
                    monsters.get(i).setActive(false);
                    monsters.get(i).setAwake(false);
                }
            }
        }
    }

    public void processCollision(List<Penguin> penguins, PolygonObstacle icicle, PooledList<Obstacle> objects){
        for (Penguin p: penguins){
            if (p.getPosition().dst(icicle.getPosition()) < 2){
                icicle.setBodyType(BodyDef.BodyType.DynamicBody);
            }
        }
    }

    public void processCollision(Water water, Player avatar){
        water.setActive(false);
        float leftX = water.getX()-((Water) water).getWidth()/2;
        float rightX = water.getX()+((Water) water).getWidth()/2;
        float downY = water.getY()-((Water) water).getHeight()/2;
        float upY = water.getY()+((Water) water).getHeight()/2;
        if (avatar.getX() >= leftX && avatar.getX() <= rightX && avatar.getY() >= downY && avatar.getY() <= upY) {
            GameplayController.hitWater(true);
        }
    }

}
