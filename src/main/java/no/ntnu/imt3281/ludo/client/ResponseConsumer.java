package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

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
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Byebye from a ResponseConsumer thread!");
    }

    void bind(MutationConsumer mutationConsumer, InputStream responseStream) {
        mMutationConsumer = mutationConsumer;
        mResponseStream = responseStream;
    }
}