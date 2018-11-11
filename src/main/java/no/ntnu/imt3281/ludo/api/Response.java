package no.ntnu.imt3281.ludo.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Response {
    public int id;
    public ResponseType type;
    public List<JSONObject> success;
    public List<JSONObject> error;
}