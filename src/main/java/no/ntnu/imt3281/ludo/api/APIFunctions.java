package no.ntnu.imt3281.ludo.api;

/**
 * Functions shared between enums
 */
public class APIFunctions {

    /**
     * Converts any enum's name to snake_case.
     * Example: 'LoginRequest' -> 'login_request'
     * 
     * @param _enum any enum
     * @return snake cased string
     */
    public static <E extends Enum<E>> String toSnakeCase(E _enum) {
        final String JavaName = _enum.name();
        String json_name = "";

        for (int i = 0; i < JavaName.length(); ++i) {
            char ch = JavaName.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    json_name += "_";
                    // ...not the first character AND upperCase
                }
            }
            json_name += Character.toLowerCase(ch);
        }
        return json_name;
    }
}