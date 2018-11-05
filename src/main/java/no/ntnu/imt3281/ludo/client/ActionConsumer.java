package no.ntnu.imt3281.ludo.client;

@FunctionalInterface
public interface ActionConsumer {
    void consume(Action action);
}
