package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Manage state in a thread safe manner.
 */
public class StateManager {

    private ArrayBlockingQueue<State> mState = new ArrayBlockingQueue<State>(1);

    public StateManager(State initialState) {
        try {
            mState.put(initialState);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }

    /**
     * Full deep copy of the current state. Blocking in case ongoing mutation
     *
     * @return state object
     */
    public State copy() {
        State copy = new State();
        try {
            State state = mState.take();
            copy = State.deepCopy(state);
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }

    /**
     * Mutate the state with provided mutation. Block all incomming copies until
     * mutation is completed.
     *
     * @param mutation which should be applied on state
     */
    public void commit(Mutation mutation) {
        try {
            State state = mState.take();
            mutation.run(state);
            mState.put(State.deepCopy(state));
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }
}
