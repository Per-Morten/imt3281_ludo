package no.ntnu.imt3281.ludo.api;

import java.util.ArrayList;

import org.json.JSONObject;

public class Response {
    public String id;
    public String type;
    public ArrayList<JSONObject> success;
    public ArrayList<JSONObject> error;
}