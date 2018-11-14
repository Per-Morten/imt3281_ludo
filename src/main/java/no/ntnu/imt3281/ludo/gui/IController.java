package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.CacheManager;

public interface IController {
    void bind(Actions actions, CacheManager state);
}
