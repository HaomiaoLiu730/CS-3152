package edu.cornell.gdiac.main.controller.gaming;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.main.view.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

public class LevelLoadingController implements ModeController {

    private Texture whiteBackground;
    private static final float SECOND_PER_FRAME = 1f/6f;
    private AssetDirectory internal;
    private FilmStrip loadingAnimation;
    private Texture loadingLetter;
    // A variable for tracking elapsed time for the animation
    float stateTime;

    public LevelLoadingController(boolean win){
        internal = new AssetDirectory( "levelLoading.json" );
        internal.loadAssets();
        internal.finishLoading();
        Texture loading;
        if(win){
            loading = internal.getEntry("normalLoading", Texture.class);
        }else{
            loading = internal.getEntry("normalLoading", Texture.class);
        }
        loadingAnimation = new FilmStrip(loading,3,3);

        whiteBackground = internal.getEntry("white", Texture.class);
        loadingLetter = internal.getEntry("loadingLetter", Texture.class);
    }

    @Override
    public void update() {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        if(stateTime > SECOND_PER_FRAME){
            stateTime %= SECOND_PER_FRAME;
            loadingAnimation.nextFrame();
        }
        // Get current frame of animation for the current stateTime
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.drawOverlay(whiteBackground, true);
        canvas.draw(loadingAnimation, Color.BLACK, loadingAnimation.getRegionWidth()/2f, loadingAnimation.getRegionHeight()/2f,
                canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.6f, 0.6f);
        canvas.draw(loadingLetter, Color.WHITE, loadingLetter.getWidth()/2f, loadingLetter.getHeight()/2f,
                canvas.getWidth()/2f, canvas.getHeight()/2f-50, 0, 0.6f, 0.6f);

    }

    @Override
    public void dispose() {
        internal.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }
}
