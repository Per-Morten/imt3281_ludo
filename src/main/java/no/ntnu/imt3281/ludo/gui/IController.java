package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

public interface IController {
    void bind(Actions actions, StateManager state);
}
