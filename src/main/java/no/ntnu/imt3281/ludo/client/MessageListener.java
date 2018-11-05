package no.ntnu.imt3281.ludo.client;

public class MessageListener implements Runnable {
    @Override
    public void run() {
        System.out.println("Hello from a MessageListener thread!");
    }
}