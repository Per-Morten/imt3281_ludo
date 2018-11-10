package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;

import java.io.InputStream;



public class ResponseConsumer implements Runnable {

    private InputStream mResponseStream;
    private MutationConsumer mMutationConsumer;

    @Override
    public void run() {
        System.out.println("Hello from a ResponseConsumer thread!");
        boolean running = true;
        while(running) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                running = false;
                Platform.exit();
                // TODO LOG ERROR
            }
        }
        System.out.println("Byebye from a ResponseConsumer thread!");
    }

    void bind(MutationConsumer mutationConsumer, InputStream responseStream) {
        mMutationConsumer = mutationConsumer;
        mResponseStream = responseStream;
    }
}