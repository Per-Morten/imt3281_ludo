package no.ntnu.imt3281.ludo.common;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.BiConsumer;

/**
 * Utility class for message parsing.
 */
public class MessageUtility {
    /**
     * Appends the given error code to the error array.
     * @param errors The array the error code should be appended to.
     * @param requestID The id of the request that failed (the one the error should be tied to).
     * @param code The error code of the error.
     */
    public static void appendError(JSONArray errors, int requestID, Error code) {
        var error = new JSONObject();
        error.put(FieldNames.ID, requestID);
        var codes = new JSONArray();
        codes.put(code.toInt());
        error.put(FieldNames.CODE, codes);
        errors.put(error);
    }

    /**
     * Appends the given json object to the successes array.
     * @param successes The array the success should be appended to.
     * @param requestID The id of the request that was a success.
     * @param success The JSONObject containing the success.
     */
    public static void appendSuccess(JSONArray successes, int requestID, JSONObject success) {
        success.put(FieldNames.ID, requestID);
        successes.put(success);
    }

    /**
     * Utility function for iterating over a JSONArray of requests,
     * that gives in the correct request ID per request.
     *
     * @param requests the JSONArray containing all the requests.
     * @param consumer A BiConsumer taking in the requestID as the first parameter,
     *                 and the actual request (the JSONObject) as the second parameter.
     */
    public static void each(JSONArray requests,
                            BiConsumer<Integer, JSONObject> consumer) {
        requests.forEach((object) -> {
            var request = (JSONObject)object;
            var requestID = request.getInt(FieldNames.ID);
            consumer.accept(requestID, request);
        });
    }
}
