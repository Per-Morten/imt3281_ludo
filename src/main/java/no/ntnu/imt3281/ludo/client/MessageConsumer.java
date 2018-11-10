package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;


public class MessageConsumer {

    private MutationConsumer mMutationConsumer;


    void bind(MutationConsumer mutationConsumer, SocketManager socketManager) {
        mMutationConsumer = mutationConsumer;
        socketManager.setOnReceiveCallback(this::feed);
    }

    private void feed(String message) {
        Logger.log(Logger.Level.DEBUG, message);
    }
}