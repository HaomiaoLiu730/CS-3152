package edu.cornell.gdiac.main;

import edu.cornell.gdiac.assets.AssetDirectory;

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
