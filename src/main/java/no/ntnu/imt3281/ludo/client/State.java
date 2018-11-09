package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.Request;
import no.ntnu.imt3281.ludo.api.Response;

/**
 * Holds all state of client
 */
public class State {
    public Request mRequest;
    public Response mResponse;    

    /**
     * Default constructor
     */
    public State() {
    	mResponse = new Response();
    	mRequest = new Request();
    }

    /**
     * Copy constructor
     * @param other state to be copied from
     */
    public State(State other) {
    	mResponse = other.mResponse;
    	mRequest = other.mRequest;
    }
}