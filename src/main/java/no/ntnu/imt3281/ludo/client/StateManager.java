package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.common.Logger;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Manage state in a thread safe manner.
 */
public class StateManager {

    private ArrayBlockingQueue<State> mState = new ArrayBlockingQueue<>(1);

    public StateManager(State initialState) {
        try {
            mState.put(initialState);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }

    /**
     * Get a copy of the userId
     *
     * @return userid logged in user
     */
    public int getUserId() {
        int copy = -1;
        try {
            State state = mState.take();
            copy = state.userId;
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }


    /**
     * Get a copy of the auth token
     *
     * @return auth token of logged in user
     */
    public String getAuthToken() {
        String copy = "";
        try {
            State state = mState.take();
            copy = state.authToken;
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
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
            copy = State.shallowCopy(state);
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }

    /**
     * Mutate the state with provided mutation. Block all incoming copies until
     * mutation is completed.
     *
     * @param mutation which should be applied on state
     */
    public void commit(Mutation mutation) {
        try {
            State state = mState.take();

            try {
                mutation.run(state);
            } catch(Exception e) {
                Logger.logException(Logger.Level.WARN, e, "Exception in mutation");
                mState.put(state);
                return;
            }
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }
}
