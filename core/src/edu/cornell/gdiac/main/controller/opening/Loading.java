package edu.cornell.gdiac.main.controller.opening;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.ControllerListener;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.main.controller.ModeController;
import edu.cornell.gdiac.util.ScreenListener;

public interface Loading extends Screen,ControllerListener {
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

    public void setScreenListener(ScreenListener listener);
}
