package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.ControllerListener;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.ModeController;

public interface Loading extends ModeController {
    /**
     * get the state of this controller mode, return true if ready
     * @return boolean
     */
    public boolean isReady();

    /**
     * get the asset directory for next mode
     * @return AssetDirectory
     */
    public AssetDirectory getAssets();
}
