package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.EventType;
import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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

    /**
     * Creates an event of the given event type, containing a payload. (which you must manually refill)
     * @param type
     * @return
     */
    public static JSONObject createEvent(EventType type) {
        var event = new JSONObject();
        event.put(FieldNames.TYPE, type);
        var payload = new JSONArray();
        event.put(FieldNames.PAYLOAD, payload);
        return event;
    }

    /**
     * Creates an event of the given type, and adds the argument to the payload.
     * @param type
     * @param argument
     */
    public static JSONObject createEvent(EventType type, JSONObject argument) {
        var event = new JSONObject();
        event.put(FieldNames.TYPE, type);
        var payload = new JSONArray();
        payload.put(argument);
        event.put(FieldNames.PAYLOAD, payload);
        return event;
    }

    /**
     * Since the JSONArray can only delete indexes, it is easier to just replace the whole thing
     * But, any existing "pointers" to the JSON array won't be replaced,
     * but rather continue to have the original array.
     *
     * This implementation was inspired by: https://stackoverflow.com/questions/47181730/remove-entries-from-jsonarray-java
     * @param requests
     * @param replacement
     */
    public static void replaceRequests(JSONArray requests, List<JSONObject> replacement) {
        while (!requests.isEmpty()) {
            requests.remove(0);
        }

        replacement.forEach(requests::put);
    }

    /**
     * Filters out all the requests in the requests parameter that does not pass through the filter.
     *
     * @param requests The requests to parse
     * @param filter The filter function used for filtering.
     */
    public static void applyFilter(JSONArray requests, BiFunction<Integer, JSONObject, Boolean> filter) {
        final var objectsToKeep = new ArrayList<JSONObject>();
        MessageUtility.each(requests, (requestID, request) -> {
            if (filter.apply(requestID, request)) {
                objectsToKeep.add(request);
            }
        });
        MessageUtility.replaceRequests(requests, objectsToKeep);
    }
}
