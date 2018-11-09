package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.util.logging.Level;

public class ResponseListener implements Runnable {
    @Override
    public void run() {
        System.out.println("Hello from a ResponseListener thread!");
        boolean running = true;
        while(running) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Byebye from a ResponseListener thread!");
    }
}