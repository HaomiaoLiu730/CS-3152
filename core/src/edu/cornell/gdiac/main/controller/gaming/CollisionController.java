package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.Obstacle;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

import java.lang.reflect.Array;
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


    public void processCollision(ArrayList<Seal> seals, ArrayList<Sealion> sealions, Player avatar, PooledList<Obstacle> objects ){
        // Monster moving and attacking
        for (Seal monster: seals) {
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
        for (Sealion monster: sealions) {
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

    public boolean processCollision(ArrayList<Seal> seals, ArrayList<Sealion> sealions, FilmStrip attackStrip, List<Penguin> penguins){
        boolean fail = false;
        for (Seal monster: seals) {
            if (monster.isActive()) {
                boolean moveMon = true;
                for(Penguin p: penguins){
                    boolean avatarBetweenX = (p.getPosition().x < avatarPos.x && avatarPos.x < monster.getPosition().x) ||
                            (p.getPosition().x > avatarPos.x && avatarPos.x > monster.getPosition().x);
                    float Xdist = Math.abs(p.getX()-monster.getX());
                    float Ydist = Math.abs(p.getY()-monster.getY());
                    if (Xdist < 0.5 && Ydist < 3 && !avatarBetweenX) {
                        moveMon = false;
                        fail = true;
                    }
                }
                if (moveMon) {
                    monster.applyForce();
                }
            }
        }
        for (Sealion monster: sealions) {
            if (monster.isActive()) {
                boolean moveMon = true;
                for(Penguin p: penguins){
                    boolean avatarBetweenX = (p.getPosition().x < avatarPos.x && avatarPos.x < monster.getPosition().x) ||
                            (p.getPosition().x > avatarPos.x && avatarPos.x > monster.getPosition().x);
                    float dist = p.getPosition().dst(monster.getPosition());
                    if (dist < 3 && !avatarBetweenX && (p.isThrowOut() || p.getIndex() == 0)) {
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

    public void processCollision(ArrayList<Seal> seals, ArrayList<Sealion> sealions, List<PolygonObstacle> icicles, PooledList<Obstacle> objects){
        for (Seal monster: seals) {
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
        for (Sealion monster: sealions) {
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
                                        PooledList<Obstacle> objects, int numPenguins, Player avatar, Sound sound, ArrayList<FilmStrip> films){
        for (Note note: notes){
            if (!note.isCollected()){
                if(avatar.getPosition().dst(note.getPosition()) <= 1 && numPenguins > 0){
                    int last_index = numPenguins - 1;
                    avatar.setNumPenguins(numPenguins - 1);
                    objects.remove(penguins.get(last_index));
                    penguins.get(last_index).setActive(false);
                    penguins.get(last_index).setAwake(false);
                    if (avatar.getNumPenguins() > 0) {
                        for (Penguin pen : avatar.getPenguins()) {
                            pen.setOverlapFilmStrip(films.get(avatar.getNumPenguins() - 1));
                        }
                    }
                    penguins.remove(penguins.get(last_index));
                    note.setFilmStrip(noteCollectedFilmStrip);
                    note.setCollected(true);
                    numNotes++;
                    sound.play();
                }else {
                    for (Penguin p : penguins) {
                        if (p.getPosition().dst(note.getPosition()) <= 1) {
                            int last_index;
                            if (!p.isThrowOut()) {
                                last_index = numPenguins - 1;
                                avatar.setNumPenguins(numPenguins - 1);
                            } else {
                                p.setActive(false);
                                p.setAwake(false);
                                p.setThrownOut(false);
                                objects.remove(p);
                                last_index = numPenguins;
                            }
                            objects.remove(penguins.get(last_index));
                            penguins.get(last_index).setActive(false);
                            penguins.get(last_index).setAwake(false);
                            if (avatar.getNumPenguins() > 0) {
                                for (Penguin pen : avatar.getPenguins()) {
                                    pen.setOverlapFilmStrip(films.get(avatar.getNumPenguins() - 1));
                                }
                            }
                            penguins.remove(penguins.get(last_index));
                            note.setFilmStrip(noteCollectedFilmStrip);
                            note.setCollected(true);
                            numNotes++;
                            sound.play();
                            break;
                        }
                    }
                }
            }
        }
        return numNotes;
    }

    private void collectNote(){

    }


    public void processCollision(List<PolygonObstacle> icicles, ArrayList<Boolean> hit, ArrayList<Integer> flag, PooledList<Obstacle> objects, Sound hitIcicel){
        for (int i = 0; i < icicles.size(); i++){
            if (hit.get(i)){
                if(!icicles.get(i).isFixedRotation())
                    hitIcicel.play();
                if (flag.get(i) == 0) {
                    icicles.get(i).setBodyType(BodyDef.BodyType.DynamicBody);
                    icicles.get(i).setFixedRotation(true);
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

    public void processCollision(List<Water> waters, List<Penguin> penguins, Player avatar){
        for (Water water: waters){
            for (Penguin p : penguins) {
                float leftX = water.getX()-((Water) water).getWidth()/2;
                float rightX = water.getX()+((Water) water).getWidth()/2;
                float downY = water.getY()-((Water) water).getHeight()/2;
                float upY = water.getY()+((Water) water).getHeight()/2;
                if (p.getX() >= leftX && p.getX() <= rightX && p.getY() >= downY && p.getY() <= upY) {
                    //p.setInWater(true);
                    if (p.isThrowOut() && !p.isGrounded()) {
                        if (avatar.getX() < p.getX()) {
                            p.setX(p.getX()-0.1f);
                        } else if (avatar.getX() > p.getX()) {
                            p.setX(p.getX()+0.1f);
                        }
                        p.setY(upY-0.2f);
                        p.setVY(0);
                    }
                    //p.setActive(false);
                    if ((avatar.isGrounded()&&!p.isThrowOut()  && !(avatar.getX() >= leftX && avatar.getX() <= rightX && avatar.isGrounded())))
                    {
                        p.setActive(false);
                        p.setY(avatar.getY() - avatar.getHeight() / 4 -0.1f);
                    }
                } else if (p.getX() >= leftX && p.getX() <= rightX) {
                    p.setActive(true);
                    //p.setInWater(false);
                }
            }
        }
    }
}