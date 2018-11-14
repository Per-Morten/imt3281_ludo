package no.ntnu.imt3281.ludo.client;

@FunctionalInterface
interface Mutation {
    void run(Cache s);
}
