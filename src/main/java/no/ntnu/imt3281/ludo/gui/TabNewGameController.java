package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.CacheManager;

public class TabNewGameController implements IController {
    private Actions mActions;
    private CacheManager mState;

    /**
     * IController
     */
    public void bind(Actions a, CacheManager s) {
        mActions = a;
        mState = s;
    }
}
