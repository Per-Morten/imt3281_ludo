package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

/**
 * Base class for all controllers
 */
public class BaseController {
    Actions mActions;
    StateManager mCache;

    void bind(Actions a, StateManager c) {

        mActions = a;
        mCache = c;
    }
}
