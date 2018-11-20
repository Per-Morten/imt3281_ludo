package no.ntnu.imt3281.ludo.client;

@FunctionalInterface
public interface Mutation {
    void run(State s);
}
