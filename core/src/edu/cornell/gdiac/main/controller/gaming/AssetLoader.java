package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;

public class AssetLoader {

    private static AssetLoader theController = null;

    /** Track all loaded assets (for unloading purposes) */
    public static Array<String> assets;

    /** The texture for the player */
    public static FilmStrip avatarStrip;
    /** The texture for the player */
    public static FilmStrip avatarNormalStrip;
    public static ArrayList<FilmStrip> penguins = new ArrayList<>();
    /** The texture for the waves */
    public static FilmStrip wavesStrip;
    /** The texture for the water */
    public static FilmStrip waterStrip;
    /** The texture for the ice */
    public static FilmStrip iceStrip;
    /** The texture for the pure white*/
    public static Texture whiteTexture;
    /** The texture for the pure black*/
    public static Texture blackTexture;
    /** The texture region for the snow */
    public static TextureRegion snowTextureRegion;
    public static TextureRegion pauseButton;
    public static TextureRegion pauseScreen;
    /** The texture region for the ice */
    public static TextureRegion iceTextureRegion;
    /** The texture region for the floating ice */
    public static TextureRegion ficeTextureRegion;
    /** The texture region for the moving ice */
    public static TextureRegion miceTextureRegion;
    /** The texture for the icicle */
    public static FilmStrip icicleStrip;
    /** The texture for the notes not collected */
    public static FilmStrip noteLeftStrip;
    /** The texture for the notes collected */
    public static FilmStrip noteCollectedStrip;
    /** The texture for the exit */
    public static FilmStrip exitStrip;
    public static FilmStrip teachThrowingStrip;
    /** The texture for the exit */
    public static TextureRegion deadStrip;


    /** The texture for the player punching */
    public static FilmStrip punchStrip;
    /** The texture for the player jumping */
    public static FilmStrip jumpRisingStrip;
    /** The texture for the player jumping */
    public static FilmStrip jumpHangingStrip;
    /** The texture for the player jumping */
    public static FilmStrip jumpLandingStrip;
    public static FilmStrip throwingStrip;

    /** The font for giving messages to the player */
    public static BitmapFont displayFont;

    public static Sound hitIcicle;
    public static Sound punching;
    public static Sound jumping;
    public static Sound throwingP;
    public static Sound collectingNote;
    public static Sound menuSellect;
    public static Sound bearLanding;
    public static Sound penguinLanding;
    public static Sound winning;
    public static Sound losing;
    public static Sound[] BackgroundMusic = new Sound[7];

    AssetDirectory directory;

    public AssetLoader(AssetDirectory directory){
        this.directory = directory;
        loadAssets(this.directory);
    }

    public static void loadAssets(AssetDirectory directory){
        avatarStrip = new FilmStrip(directory.getEntry("avatarWalking", Texture.class), 1, 18);
        penguins.add(new FilmStrip(directory.getEntry("penguin", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin2", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin3", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin4", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin5", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin6", Texture.class), 1, 1));
        penguins.add(new FilmStrip(directory.getEntry("penguin7", Texture.class), 1, 1));
        avatarNormalStrip = new FilmStrip(directory.getEntry("avatarNormal", Texture.class), 1, 1);
        icicleStrip = new FilmStrip(directory.getEntry("icicle", Texture.class), 1, 1);
        exitStrip = new FilmStrip(directory.getEntry("exit", Texture.class), 1, 1);
        punchStrip = new FilmStrip(directory.getEntry("avatarPunching", Texture.class), 1, 1);
        noteLeftStrip = new FilmStrip(directory.getEntry("notcollected", Texture.class), 1, 1);
        noteCollectedStrip = new FilmStrip(directory.getEntry("collected", Texture.class), 1, 1);
        jumpRisingStrip = new FilmStrip(directory.getEntry("jumpRising", Texture.class), 1, 4);
        jumpHangingStrip = new FilmStrip(directory.getEntry("jumpHanging", Texture.class), 1, 1);
        jumpLandingStrip = new FilmStrip(directory.getEntry("jumpLanding", Texture.class), 1, 3);
        throwingStrip = new FilmStrip(directory.getEntry("avatarThrow", Texture.class), 1, 6);
        icicleStrip = new FilmStrip(directory.getEntry("icicle", Texture.class), 10, 165);
        iceStrip= new FilmStrip(directory.getEntry("ice", Texture.class), 1, 1);
        snowTextureRegion = new TextureRegion(directory.getEntry("snow", Texture.class));
        iceTextureRegion = new TextureRegion(directory.getEntry("ice", Texture.class));
        waterStrip =new FilmStrip(directory.getEntry("water", Texture.class), 1, 1);
        wavesStrip = new FilmStrip(directory.getEntry("waves", Texture.class), 1, 4);
        ficeTextureRegion = new TextureRegion(directory.getEntry("floatingIce", Texture.class));
        miceTextureRegion = new TextureRegion(directory.getEntry("ice", Texture.class));
        whiteTexture = directory.getEntry("white",Texture.class);
        blackTexture = directory.getEntry("black",Texture.class);
        pauseButton = new TextureRegion(directory.getEntry("pauseButton", Texture.class));
        pauseScreen = new TextureRegion(directory.getEntry("pauseScreen",Texture.class));
        teachThrowingStrip = new FilmStrip(directory.getEntry("teachThrowing", Texture.class), 12, 8);;
        deadStrip = new TextureRegion(directory.getEntry("dead",Texture.class));

        hitIcicle = directory.getEntry("hitIcicle", SoundBuffer.class);
        punching = directory.getEntry("punching", SoundBuffer.class);
        jumping = directory.getEntry("jumping", SoundBuffer.class);
        throwingP = directory.getEntry("throwingP", SoundBuffer.class);
        collectingNote = directory.getEntry("collectingNote", SoundBuffer.class);
        menuSellect = directory.getEntry("menuSellect", SoundBuffer.class);
        bearLanding = directory.getEntry("bearLanding", SoundBuffer.class);
        penguinLanding = directory.getEntry("penguinLanding", SoundBuffer.class);
        winning = directory.getEntry("winning", SoundBuffer.class);
        losing = directory.getEntry("losing",SoundBuffer.class);

        BackgroundMusic[0] = directory.getEntry("l1",SoundBuffer.class);
        BackgroundMusic[1] = directory.getEntry("l2",SoundBuffer.class);
        BackgroundMusic[2] = directory.getEntry("l3",SoundBuffer.class);
        BackgroundMusic[3] = directory.getEntry("l4",SoundBuffer.class);
        BackgroundMusic[4] = directory.getEntry("l5",SoundBuffer.class);
        BackgroundMusic[5] = directory.getEntry("l6",SoundBuffer.class);
        BackgroundMusic[6] = directory.getEntry("l7",SoundBuffer.class);

    }

}
