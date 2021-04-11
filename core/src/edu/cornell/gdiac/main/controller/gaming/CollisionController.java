package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.Obstacle;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

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


    public void processCollision(Monster monster, Player avatar, PooledList<Obstacle> objects ){
        // Monster moving and attacking
        distMonsterAvatar = avatar.getPosition().dst(monster.getPosition());
        if (avatar.isPunching()) {
            if (distMonsterAvatar < 3) {
                monster.setActive(false);
                monster.setAwake(false);
                objects.remove(monster);
            }
        }
    }

    public void processCollision(Monster monster, FilmStrip attackStrip, List<Penguin> penguins){
        if (monster.isActive()) {
            boolean moveMon = true;
            for(Penguin p: penguins){
                float dist2 = p.getPosition().dst(monster.getPosition());
                if (dist2 < 3 && dist2 < distMonsterAvatar) {
                    monster.setFilmStrip(attackStrip);
                    if (p.getPosition().x < monster.getPosition().x) {
                        monster.setFacingRight(-1);
                    }
                    moveMon = false;
                    GameplayController.resetCountDown -= 1;
                }
            }
            if (moveMon) {
                monster.applyForce();
            }
        }
    }

    public void processCollision(Monster monster, PolygonObstacle icicle, PooledList<Obstacle> objects){
        if (icicle.getPosition().dst(monster.getPosition()) <= 1){
            objects.remove(monster);
            monster.setActive(false);
            monster.setAwake(false);
        }
    }

    public int processCollision(List<Penguin> penguins, Note note, FilmStrip noteCollectedFilmStrip, int numNotes,
                                 PooledList<Obstacle> objects, int numPenguins, Player avatar){
        if (!note.isCollected()){
            for (Penguin p: penguins){
                if (p.getPosition().dst(note.getPosition()) <= 1){
                    note.setFilmStrip(noteCollectedFilmStrip);
                    note.setCollected(true);
                    numNotes ++;
                    objects.remove(p);
                    p.setActive(false);
                    p.setAwake(false);
                    avatar.getPenguins().remove(p.getIndex());
                    if (!p.isThrowOut())avatar.setNumPenguins(numPenguins - 1);
                    avatar.resetPenguinIndex(avatar.getPenguins());
                }
            }
        }
        return numNotes;
    }

    public int processCollision(Note note, FilmStrip noteCollectedFilmStrip, int numNotes, Player avatar){
        if (!note.isCollected()){
            if (avatar.getPosition().dst(note.getPosition()) <= 1){
                note.setFilmStrip(noteCollectedFilmStrip);
                note.setCollected(true);
                numNotes ++;
            }
        }
        return numNotes;
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
