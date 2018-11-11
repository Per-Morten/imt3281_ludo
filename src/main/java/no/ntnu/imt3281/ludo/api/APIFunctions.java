package no.ntnu.imt3281.ludo.api;

/**
 * Functions shared between enums
 */
class APIFunctions {

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
    static ResponseType fromSnakeCase(String response_name) {
        StringBuilder ResponseName = new StringBuilder();

        ResponseName.append(String.valueOf(response_name.charAt(0)).toUpperCase());

        for (int i = 0; i < response_name.length(); ++i) {

            if (response_name.charAt(i) == '_') {
                ++i;
                ResponseName.append(String.valueOf(response_name.charAt(i)).toUpperCase());
                continue;
            }

            ResponseName.append(response_name.charAt(i));

        }
        return ResponseType.valueOf(ResponseName.toString());
    }
 }