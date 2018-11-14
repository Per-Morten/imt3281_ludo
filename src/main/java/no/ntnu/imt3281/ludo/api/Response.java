package no.ntnu.imt3281.ludo.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Response {
    public int id;
    public ResponseType type;
    public List<JSONObject> success;
    public List<JSONObject> error;


    /**
     * Make reponse object from string.
     * already.
     *
     * @param json json response object
     * @throws IllegalArgumentException if type does not match any ResponseType
     * @throws JSONException if any json keys does not match the specification of a response
     *
     * @return Response object
     */
    static public Response fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {

        Response res = new Response();

        res.id = json.getInt("id");
        String responseTypeStr = json.getString("type");
        res.type = ResponseType.fromString(responseTypeStr);

        var successList = new ArrayList<JSONObject>();
        JSONArray successArray = json.getJSONArray("success");
        for (int i = 0; i < successArray.length(); ++i) {
            JSONObject o = successArray.getJSONObject(i);
            successList.add(o);
        }
        res.success = successList;

        var errorList = new ArrayList<JSONObject>();
        JSONArray errorArray = json.getJSONArray("error");
        for (int i = 0; i < errorArray.length(); ++i) {
            JSONObject o = errorArray.getJSONObject(i);
            errorList.add(o);
        }
        res.error = errorList;

        return res;
    }
}