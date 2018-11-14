package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Functions shared between enums
 */
public class APIFunctions {

    /**
     * Converts RequestType enum to snake_case
     * Example: 'LoginRequest' -> 'login_request'
     * 
     * @param type enum to be converted
     * @return snake cased string
     */
    static public String toSnakeCase(RequestType type) {
        final String RequestName = type.name();
        StringBuilder request_name = new StringBuilder();

        for (int i = 0; i < RequestName.length(); ++i) {
            char ch = RequestName.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    request_name.append("_");
                    // ...not the first character AND upperCase
                }
            }
            request_name.append(Character.toLowerCase(ch));
        }
        return request_name.toString();
    }

    /**
     * Convert snake_cased string to PascalCased enum
     *
     * @param response_name snake_cased
     * @return string which could be responseType or eventType
     */
    static public String fromSnakeCase(String response_name) {
        StringBuilder ResponseName = new StringBuilder();

        ResponseName.append(String.valueOf(response_name.charAt(0)).toUpperCase());

        for (int i = 1; i < response_name.length(); ++i) {

            if (response_name.charAt(i) == '_') {
                ++i;
                ResponseName.append(String.valueOf(response_name.charAt(i)).toUpperCase());
                continue;
            }

            ResponseName.append(response_name.charAt(i));
        }
        return ResponseName.toString();
    }

    /**
     * Make reponse object from string
     *
     * @param jsonres json response object
     *
     * @return Response object
     */
    static public Response makeResponse(JSONObject jsonres) throws JSONException, ClassCastException {

        Response res = new Response();

        res.id = jsonres.getInt("id");
        String resopnseType = APIFunctions.fromSnakeCase(jsonres.getString("type"));
        res.type = ResponseType.valueOf(resopnseType);

        var successList = new ArrayList<JSONObject>();
        JSONArray successArray = jsonres.getJSONArray("success");
        for (int i = 0; i < successArray.length(); ++i) {
            JSONObject o = successArray.getJSONObject(i);
            successList.add(o);
        }
        res.success = successList;

        var errorList = new ArrayList<JSONObject>();
        JSONArray errorArray = jsonres.getJSONArray("error");
        for (int i = 0; i < errorArray.length(); ++i) {
            JSONObject o = errorArray.getJSONObject(i);
            errorList.add(o);
        }
        res.error = errorList;

        return res;
    }

    /**
     * Check if string matches any response type enum
     *
     * @param maybeResponseType pascal cased response type
     *
     * @return true or false if any match found
     */
    static public boolean isResponseType(String maybeResponseType) {
        var values = ResponseType.values();
        return Arrays.stream(values).anyMatch(value -> value.toString().compareTo(maybeResponseType) == 0);
    }

    /**
     * See if string matches any event type enum
     *
     * @param maybeEventType pascal cased response type
     *
     * @return true if the string match any enum
     */
    static public boolean isEventType(String maybeEventType) {
        var values = EventType.values();
        return Arrays.stream(values).anyMatch(value -> value.toString().compareTo(maybeEventType) == 0);
    }
 }