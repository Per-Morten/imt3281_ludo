package no.ntnu.imt3281.ludo.common;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONArray;
import org.json.JSONObject;

public class MessageUtility {
    public static void addErrorToArray(JSONArray errors, int requestID, Error code) {
        var error = new JSONObject();
        error.put(FieldNames.ID, requestID);
        var codes = new JSONArray();
        codes.put(code);
        error.put(FieldNames.CODE, codes);
        errors.put(error);
    }
}
