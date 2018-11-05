package no.ntnu.imt3281.ludo.api;

import java.util.ArrayList;

import org.json.JSONObject;

public class Request {
    public String id;
    public String type;
    public String token;
    public ArrayList<JSONObject> payload;
}