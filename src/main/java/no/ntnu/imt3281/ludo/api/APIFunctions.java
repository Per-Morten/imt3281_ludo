package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Functions shared between enums
 */
public class APIFunctions {

    /**
     * Converts RequestType enum to snake case
     * Example: 'LoginRequest' -> 'login_request'
     * 
     * @param type enum to be converted
     * @return snake cased string
     */
    static String toSnakeCase(RequestType type) {
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
     * Convert snake_cased string to ResponseType enum
     *
     * @param response_name snake_cased
     * @return responseType enum
     */
    static ResponseType fromSnakeCase(String response_name) throws IllegalArgumentException {
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
        return ResponseType.valueOf(ResponseName.toString());
    }

    /**
     * Make reponse object from string
     *
     * @param message json object as string
     * @return Response object
     */
    static public Response makeResponse(String message) throws JSONException, IllegalArgumentException {

        Response res = new Response();
        JSONObject json = new JSONObject(message);

        res.id = json.getInt("id");
        res.type = APIFunctions.fromSnakeCase(json.getString("type"));
        res.success = json.getJSONArray("success");
        res.error= json.getJSONArray("error");

        return res;
    }
 }