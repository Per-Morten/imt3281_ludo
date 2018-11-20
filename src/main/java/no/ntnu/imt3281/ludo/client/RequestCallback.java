package no.ntnu.imt3281.ludo.client;

import org.json.JSONArray;
import org.json.JSONObject;

@FunctionalInterface
public interface RequestCallback {
    void run(JSONObject responseItem);
}
