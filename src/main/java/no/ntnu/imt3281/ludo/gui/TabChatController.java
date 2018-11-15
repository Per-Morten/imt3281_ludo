package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.CacheManager;

public class TabChatController implements IController {
    private Actions mActions;
    private CacheManager mCache;

    /**
     * IController
     */
    public void bind(Actions a, CacheManager c) {

        mActions = a;
        mCache = c;
    }
}
