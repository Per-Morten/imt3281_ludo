package no.ntnu.imt3281.ludo.api;

import org.json.JSONArray;

public class Response {
    public int id;
    public ResponseType type;
    public JSONArray success;
    public JSONArray error;
}