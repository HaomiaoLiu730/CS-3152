package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.GDXRoot;
import edu.cornell.gdiac.main.model.*;
import edu.cornell.gdiac.main.obstacle.BoxObstacle;
import edu.cornell.gdiac.main.obstacle.PolygonObstacle;

import java.util.ArrayList;
import java.util.logging.Level;

public class LevelLoader {

    private AssetDirectory internal;
    private JsonValue constants;
    public AssetLoader assetLoader = GDXRoot.assetLoader;
    public Vector2 scale = new Vector2(40f, 40f);
    public Texture background;
    public BitmapFont gameFont;
    /** number of penguins */
    public int num_penguins;
    /** number of notes */
    public int num_notes;
    private int level;
    public float[] grounded;
    public String jsonFile;

    ArrayList<PolygonObstacle> snowList = new ArrayList<>();
    ArrayList<PolygonObstacle> iciclesList = new ArrayList<>();
    BoxObstacle exit;
    Player avatar;
    ArrayList<Note> notesList = new ArrayList<Note>();
    ArrayList<Water> waterList = new ArrayList<Water>();
    ArrayList<Ice> iceList = new ArrayList<Ice>();
    ArrayList<FloatingIce> floatingIcesList = new ArrayList<FloatingIce>();
    ArrayList<MovingIce> movingIcesList = new ArrayList<MovingIce>();

    public LevelLoader reset(){
        return new LevelLoader(jsonFile, level);
    }

    public LevelLoader(String jsonFile, int level){
        this.jsonFile = jsonFile;
        internal = new AssetDirectory(jsonFile);
        internal.loadAssets();
        internal.finishLoading();
        background = internal.getEntry("background", Texture.class);
        gameFont = internal.getEntry("gameFont", BitmapFont.class);
        this.level = level;
        constants = internal.getEntry( "level"+(level+1), JsonValue.class );
        JsonValue defaults = constants.get("defaults");
        num_penguins = defaults.getInt("num_penguins",0);
        num_notes = defaults.getInt("num_notes",0);
        grounded = defaults.get("grounded").asFloatArray();
        loadSnow();
        loadIcicles();
        loadGoal();
        loadPlayer();
        loadNotes();
        loadWater();
        loadIce();
        loadFloatingIces();
        loadMovingIce();
    }

    public void loadSnow(){
        // Add level goal
        float dwidth, dheight;
        JsonValue defaults = constants.get("defaults");
        String sname = "snow";
        for (int ii = 0; ii < defaults.get("snow").size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(defaults.get("snow").get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0));
            obj.setFriction(defaults.getFloat("friction", 0));
            obj.setRestitution(defaults.getFloat("restitution", 0));
            obj.setDrawScale(scale);
            obj.setTexture(assetLoader.snowTextureRegion);
            obj.setName(sname+ii);
            snowList.add(obj);
        }
    }

    public void loadIcicles(){
        JsonValue icicles = constants.get("icicles");
        JsonValue iciclepos = icicles.get("pos");

        for (int i = 0; i < icicles.get("pos").size; i ++){
            PolygonObstacle icicle;
            icicle = new PolygonObstacle(icicles.get("layout").get(i).asFloatArray(), iciclepos.get(i).getFloat(0), iciclepos.get(i).getFloat(1));
            icicle.setBodyType(BodyDef.BodyType.StaticBody);
            icicle.setDensity(icicles.getFloat("density"));
            icicle.setFriction(0);
            icicle.setRestitution(icicles.getFloat("restitution"));
            icicle.setDrawScale(scale);
            icicle.setTexture(assetLoader.icicleStrip);
            icicle.setName("icicle" + i);
            iciclesList.add(icicle);
        }
    }

    public void loadGoal(){
        JsonValue goal = constants.get("goal");
        JsonValue goalpos=goal.get("pos");
        BoxObstacle exit;
        if ((jsonFile.startsWith("europe") && GDXRoot.numOfLevels.get(MenuController.Continent.Europe) == getLevel()+1) ||
                (jsonFile.startsWith("africa") && GDXRoot.numOfLevels.get(MenuController.Continent.Africa) == getLevel()+1) ||
                (jsonFile.startsWith("oceania") && GDXRoot.numOfLevels.get(MenuController.Continent.Oceania) == getLevel()+1) ||
                (jsonFile.startsWith("asia") && GDXRoot.numOfLevels.get(MenuController.Continent.Asia) == getLevel()+1) ||
                (jsonFile.startsWith("northAmerica") && GDXRoot.numOfLevels.get(MenuController.Continent.NorthAmerica) == getLevel()+1) ||
                (jsonFile.startsWith("southAmerica") && GDXRoot.numOfLevels.get(MenuController.Continent.SouthAmerica) == getLevel()+1) ||
                (jsonFile.startsWith("antarctica") && GDXRoot.numOfLevels.get(MenuController.Continent.Antarctica) == getLevel()+1)) {
            this.exit = new BoxObstacle( goalpos.getFloat(0), goalpos.getFloat(1), goal.getFloat(("width")), goal.getFloat(("height")));
            this.exit.setTexture(assetLoader.cageStrip);
        } else {
            this.exit = new BoxObstacle( goalpos.getFloat(0), goalpos.getFloat(1)+0.25f, goal.getFloat(("width")), goal.getFloat(("height")));
            this.exit.setTexture(assetLoader.exitStrip);
        }
        //this.exit = new BoxObstacle( goalpos.getFloat(0), goalpos.getFloat(1)+0.25f, goal.getFloat(("width")), goal.getFloat(("height")));
        this.exit.setBodyType(BodyDef.BodyType.StaticBody);
        this.exit.setSensor(true);
        this.exit.setDensity(goal.getFloat("density"));
        this.exit.setFriction(goal.getFloat("friction"));
        this.exit.setRestitution(goal.getFloat("restitution"));
        this.exit.setDrawScale(scale);
        this.exit.setName("exit");
    }

    public void loadPlayer(){
        // add object for both player and pengins
        float dwidth  = assetLoader.avatarStrip.getRegionWidth()/scale.x;
        float dheight = assetLoader.avatarStrip.getRegionHeight()/scale.y;
        float PUNCH_COOLDOWN=constants.get("player").getInt("punch_cool")/2;
        float PUNCH_TIME=constants.get("player").getInt("punch_time");
        float punchCooldown=constants.get("player").getInt("punch_cooldown");
        avatar = new Player(constants.get("player"),constants.get("penguins"), dwidth, dheight-0.5f, num_penguins, assetLoader.penguins);
        avatar.setDrawScale(scale);
        avatar.setFilmStrip(assetLoader.avatarStrip);
        avatar.setJumpHangingStrip(assetLoader.jumpHangingStrip);
        avatar.setJumpLandingStrip(assetLoader.jumpLandingStrip);
        avatar.setJumpRisingStrip(assetLoader.jumpRisingStrip);
        avatar.setWalkingStrip(assetLoader.avatarStrip);
        avatar.setThrowingStrip(assetLoader.throwingStrip);
        avatar.setNormalStrip(assetLoader.avatarNormalStrip);
        avatar.setPenguinWalkingStrip((assetLoader.penguinWalkingStrip));
        avatar.setPenguinRollingStrip(assetLoader.penguinRollingStrip);
        avatar.setPenguinStrip(assetLoader.penguins.get(0));
        avatar.setPenguinOverlapStrip(assetLoader.penguins.get(avatar.getNumPenguins()-1));
        for(int i = 0; i<num_penguins; i++){
            avatar.getPenguins().get(i).setDrawScale(scale);
            avatar.getPenguins().get(i).setWalkingStrip(assetLoader.penguinWalkingStrip);
            avatar.getPenguins().get(i).setRolllingFilmStrip(assetLoader.penguinRollingStrip);
            avatar.getPenguins().get(i).setFilmStrip(assetLoader.penguinWalkingStrip);
            avatar.getPenguins().get(i).setOverlapFilmStrip(assetLoader.penguins.get(avatar.getNumPenguins()-1));
            avatar.getPenguins().get(i).setOverlapFilmStrip(assetLoader.penguins.get(avatar.getNumPenguins()-1));
        }
        avatar.pseudoPenguin.setDrawScale(scale);
        avatar.pseudoPenguin.setWalkingStrip(assetLoader.penguinWalkingStrip);
        avatar.pseudoPenguin.setRolllingFilmStrip(assetLoader.penguinRollingStrip);
        avatar.pseudoPenguin.setFilmStrip(assetLoader.penguinWalkingStrip);
        avatar.pseudoPenguin.setOverlapFilmStrip(assetLoader.penguins.get(avatar.getNumPenguins()-1));
    }

    public void loadNotes(){
        JsonValue notes = constants.get("notes");
        JsonValue notespos = notes.get("pos");
        for (int i =0; i< notespos.size; i++) {
            Note note = new Note(notes, assetLoader.noteLeftStrip.getRegionWidth() / scale.x, assetLoader.noteLeftStrip.getRegionHeight() / scale.y, i );
            note.setFilmStrip(assetLoader.noteLeftStrip);
            note.setDrawScale(scale);
            notesList.add(note);
        }
    }

    public void loadWater(){
        JsonValue waters = constants.get("water");
        JsonValue water_layout = waters.get("layout");
        waterList= new ArrayList<Water>();
        for (int i =0; i< water_layout.size; i++) {
            Water water = new Water(waters, water_layout.get(i).getFloat(0),water_layout.get(i).getFloat(1), "water",i);
            water.setFilmStrip(assetLoader.waterStrip, assetLoader.wavesStrip);
            water.setDrawScale(scale);
            waterList.add(water);
//            water.setBodyType(BodyDef.BodyType.StaticBody);
//            water.setSensor(true);
//            water.setActive(false);
//            water.setAwake(false);
        }
    }

    public void loadIce(){
        JsonValue ices = constants.get("ice");
        JsonValue icepos = ices.get("pos");
        Ice ice;
        for (int i =0; i< icepos.size; i++) {
            int w = ices.get("layout").get(i).getInt(0);
            int h = ices.get("layout").get(i).getInt(1);
            ice = new Ice(ices, i, w/scale.x, h/scale.y);
            TextureRegion temp = new TextureRegion(assetLoader.iceTextureRegion);
            temp.setRegionWidth(w);
            temp.setRegionHeight(h);
            ice.setDrawScale(scale);
            ice.setTexture(temp);
            ice.setRestitution(ices.getFloat("restitution"));
            iceList.add(ice);
        }
    }

    public void loadFloatingIces(){
        JsonValue fices = constants.get("floatingIce");
        JsonValue ficepos = fices.get("pos");
        FloatingIce fIce;
        for (int i =0; i< ficepos.size; i++) {
            int w = fices.get("layout").get(i).getInt(0);
            int h = fices.get("layout").get(i).getInt(1);
            fIce = new FloatingIce(fices, i, w/scale.x, h/scale.y);
            TextureRegion temp = new TextureRegion(assetLoader.ficeTextureRegion);
            temp.setRegionWidth(w);
            temp.setRegionHeight(h);
            fIce.setDrawScale(scale);
            fIce.setTexture(temp);
            fIce.setRestitution(fices.getFloat("restitution"));
            floatingIcesList.add(fIce);
        }
    }

    public void loadMovingIce(){
        JsonValue mices = constants.get("movingIce");
        JsonValue micepos = mices.get("pos");
        MovingIce mIce;
        for (int i =0; i< micepos.size; i++) {
            int w = mices.get("layout").get(i).getInt(0);
            int h = mices.get("layout").get(i).getInt(1);
            mIce = new MovingIce(mices, i, w/scale.x, h/scale.y);
            TextureRegion temp = new TextureRegion(assetLoader.miceTextureRegion);
            temp.setRegionWidth(w);
            temp.setRegionHeight(h);
            mIce.setDrawScale(scale);
            mIce.setTexture(temp);

            mIce.setRestitution(mices.getFloat("restitution"));
            movingIcesList.add(mIce);
        }
    }

    public int getLevel(){
        return level;
    }
}
