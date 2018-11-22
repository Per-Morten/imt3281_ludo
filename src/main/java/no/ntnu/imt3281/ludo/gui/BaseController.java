package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

/**
 * Base class for all controllers
 */
public class BaseController {
    Actions mActions;

    void bind(Actions a) {
        mActions = a;
    }
}
