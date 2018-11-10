package no.ntnu.imt3281.ludo.client;

@FunctionalInterface
public interface Action {
    void run(ActionConsumer a);
}
