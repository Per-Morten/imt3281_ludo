package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

public class UserController implements IController {
    private Actions mActions;
    private StateManager mCache;

    /**
     * IController
     */
    public void bind(Actions a, StateManager c) {

        mActions = a;
        mCache = c;
    }
}
