package no.ntnu.imt3281.ludo.client;

@FunctionalInterface
public interface MutationConsumer {
    public void consume(Mutation mutation);
}
