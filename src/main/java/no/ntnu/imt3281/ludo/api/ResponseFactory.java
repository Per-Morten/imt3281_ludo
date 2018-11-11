package no.ntnu.imt3281.ludo.api;

import org.json.JSONException;
import org.json.JSONObject;


public class ResponseFactory {


    /**
     * Make reponse object from string
     *
     * @param message json object as string
     * @return Response object
     */
    public Response makeResponse(String message) throws JSONException {

        Response res = new Response();
        JSONObject json = new JSONObject(message);
        res.id = json.getInt("id");
        //res.type = APIFunctions.fromSnakeCase(json.getString("type"));
        res.success = json.getJSONArray("success");
        res.error= json.getJSONArray("error");
        return res;
    }


}