package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;

@SuppressWarnings("serial")
public class APIErrorException extends RuntimeException {
    Error mError;

    public APIErrorException(Error error) {
        mError = error;
    }

    public Error getError() {
        return mError;
    }
}

