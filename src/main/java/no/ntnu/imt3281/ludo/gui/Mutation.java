package no.ntnu.imt3281.ludo.gui;

@FunctionalInterface
public interface Mutation {
    void run(MutationConsumer m);
}