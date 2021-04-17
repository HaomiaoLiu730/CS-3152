package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.math.Vector2;
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

    private Vector2 avatarPos;

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
        for (Monster monster: monsters) {
            if (monster.isActive()) {
                avatarPos = avatar.getPosition();
                float dist = avatar.getPosition().dst(monster.getPosition());
                if (avatar.isPunching()) {
                    if (dist < 3) {
                        monster.setActive(false);
                        monster.setAwake(false);
                        objects.remove(monster);
                    }
                }
            }
        }
    }

    public boolean processCollision(ArrayList<Monster> monsters, FilmStrip attackStrip, List<Penguin> penguins){
        boolean fail = false;
        for (Monster monster: monsters) {
            if (monster.isActive()) {
                boolean moveMon = true;
                for(Penguin p: penguins){
                    boolean avatarBetweenX = (p.getPosition().x < avatarPos.x && avatarPos.x < monster.getPosition().x) ||
                            (p.getPosition().x > avatarPos.x && avatarPos.x > monster.getPosition().x);
                    float dist = p.getPosition().dst(monster.getPosition());
                    if (dist < 3 && !avatarBetweenX) {
                        monster.setFilmStrip(attackStrip);
                        if (p.getPosition().x < monster.getPosition().x) {
                            monster.setFacingRight(-1);
                        }
                        moveMon = false;
                        fail = true;
                    }
                }
                if (moveMon) {
                    monster.applyForce();
                }
            }
        }
        return fail;
    }

    public void processCollision(ArrayList<Monster> monsters, List<PolygonObstacle> icicles, PooledList<Obstacle> objects){
        for (Monster monster: monsters) {
            if (monster.isActive()) {
                for (PolygonObstacle icicle: icicles){
                    if (icicle.getPosition().dst(monster.getPosition()) <= 1){
                        objects.remove(monster);
                        monster.setActive(false);
                        monster.setAwake(false);
                    }
                }
            }
        }
    }

    public int penguin_note_interaction(List<Penguin> penguins, List<Note> notes, FilmStrip noteCollectedFilmStrip, int numNotes,
                                        PooledList<Obstacle> objects, int numPenguins, Player avatar){
        for (Note note: notes){
            if (!note.isCollected()){
                for (Penguin p: penguins){
                    if (p.getPosition().dst(note.getPosition()) <= 1) {
                        int last_index;
                        if (!p.isThrowOut()){
                            last_index = numPenguins - 1;
                            avatar.setNumPenguins(numPenguins - 1);
                        } else {
                            last_index = numPenguins ;
                        }
                        objects.remove(penguins.get(last_index));
                        penguins.get(last_index).setActive(false);
                        penguins.get(last_index).setAwake(false);
                        avatar.getPenguins().remove(last_index);
                        note.setFilmStrip(noteCollectedFilmStrip);
                        note.setCollected(true);
                        numNotes++;
                        break;
                    }
                }
            }
        }
        return numNotes;
    }


    public void processCollision(List<Penguin> penguins, List<PolygonObstacle> icicles, PooledList<Obstacle> objects){
        for (Penguin p: penguins){
            for (PolygonObstacle icicle: icicles){
                if (p.getPosition().dst(icicle.getPosition()) < 2){
                    icicle.setBodyType(BodyDef.BodyType.DynamicBody);
                    icicle.setFixedRotation(true);
                }
            }
        }
    }

    public void processCollision(List<Water> waters, Player avatar){
        for (Water water: waters){
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
}