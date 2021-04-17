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

    public boolean processCollision(ArrayList<Monster> monsters, FilmStrip attackStrip, List<Penguin> penguins){
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
                        return true;
                    }
                }
                if (moveMon) {
                    monsters.get(i).applyForce();
                }
            }
        }
        return false;
    }

    public void processCollision(ArrayList<Monster> monsters, List<PolygonObstacle> icicles, PooledList<Obstacle> objects){
        for (int i = 0; i < monsters.size(); i++) {
            if (monsters.get(i).isActive()) {
                for (PolygonObstacle icicle: icicles){
                    if (icicle.getPosition().dst(monsters.get(i).getPosition()) <= 1){
                        objects.remove(monsters.get(i));
                        monsters.get(i).setActive(false);
                        monsters.get(i).setAwake(false);
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


//    public void processCollision(List<Penguin> penguins, List<PolygonObstacle> icicles, PooledList<Obstacle> objects){
//        for (Penguin p: penguins){
//            for (PolygonObstacle icicle: icicles){
//                if (p.getPosition().dst(icicle.getPosition()) < 2){
//                    icicle.setBodyType(BodyDef.BodyType.DynamicBody);
//                    icicle.setFixedRotation(true);
//                }
//            }
//        }
//    }

    public static ArrayList<Boolean> if_icicle_grounded(ArrayList<PolygonObstacle> snow_list,
                                                        ArrayList<PolygonObstacle> iciclesList, ArrayList<Boolean> icicle_ground,
                                                        float[] grounded){
        for (int i = 0; i<iciclesList.size(); i++){
            if (!icicle_ground.get(i)){
                for (PolygonObstacle snow: snow_list){
                    int index = Integer.parseInt(snow.getName().substring(snow.getName().length()-1));
                    for (float f: grounded){
                        if(f == index){
                            PolygonObstacle icicle = iciclesList.get(i);
                            System.out.println(pDistance(icicle.getX(), icicle.getY() - icicle.getHeight()/2, snow.getX(),
                                    snow.getY() + snow.getHeight(), snow.getX() + snow.getWidth(), snow.getY() + snow.getHeight()));
                            if (pDistance(icicle.getX(), icicle.getY() - icicle.getHeight()/2, snow.getX() - snow.getWidth()/2,
                                    snow.getY() + snow.getHeight()/2, snow.getX() + snow.getWidth()/2, snow.getY() + snow.getHeight()/2)
                                    <= 0){
                                icicle_ground.set(i, true);
                            }
                        }
                    }
                }
            }
        }
        return icicle_ground;
    }

    /** Returns the distance between point (x,y) and a line composed by point (x1,y1) and (x2,y2).*/
    public static float pDistance(float x, float y, float x1, float y1, float x2, float y2) {

        float A = x - x1; // position of point rel one end of line
        float B = y - y1;
        float C = x2 - x1; // vector along line
        float D = y2 - y1;
        float E = -D; // orthogonal vector
        float F = C;

        float dot = A * E + B * F;
        float len_sq = E * E + F * F;

        return (float) ((float) Math.abs(dot) / Math.sqrt(len_sq));
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