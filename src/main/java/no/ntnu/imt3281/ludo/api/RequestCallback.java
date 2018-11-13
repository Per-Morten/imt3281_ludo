package no.ntnu.imt3281.ludo.api;

import org.json.JSONArray;
import org.json.JSONObject;

@FunctionalInterface
public interface RequestCallback {
    public void run(JSONObject requestItem, JSONObject responseItem);
}
