package no.ntnu.imt3281.ludo.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.api.ResponseType;
import no.ntnu.imt3281.ludo.common.NetworkConfig;

public class SocketManagerTest {
    @Test
    public void CanShutdownWithoutHavingHadClients() throws InterruptedException {
        var manager = new SocketManager(NetworkConfig.LISTENING_PORT);
        manager.start();
        manager.stop();
    }

    @Test
    public void GetUserIDWhenCreatingUser() throws IOException, InterruptedException {
        final AtomicBoolean running = new AtomicBoolean(true);

        var server = new SocketManager(NetworkConfig.LISTENING_PORT);
        server.setOnReceiveCallback((msg) -> {
            var val = msg.message;
            var json = new JSONObject(val);
            var msgID = json.getLong("id");
            assertEquals(msgID, 0);
            var type = json.getString("type");
            assertEquals(type, RequestType.CREATE_USER_REQUEST.toLowerCaseString());

            var payload = json.getJSONArray("payload");
            var request = payload.getJSONObject(0);
            var payloadID = request.getInt("id");
            assertEquals(payloadID, 0);

            var response = new JSONObject();
            response.put("id", 0);
            response.put("type", ResponseType.CREATE_USER_RESPONSE.toLowerCaseString());
            var success = new JSONArray();
            var firstSuccess = new JSONObject();
            firstSuccess.put("id", 0);
            firstSuccess.put("user_id", 2);
            success.put(0, firstSuccess);
            response.put("success", success);
            response.put("error", new JSONArray());

            server.send(msg.socketID, response.toString());
        });
        server.start();

        var client = new no.ntnu.imt3281.ludo.client.SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);
        client.setOnReceiveCallback((value) -> {
            var json = new JSONObject(value);
            var msgID = json.getInt("id");
            assertEquals(msgID, 0);
            var type = json.getString("type");
            assertEquals(type, ResponseType.CREATE_USER_RESPONSE.toLowerCaseString());
            var success = json.getJSONArray("success");
            assert (success.length() == 1);
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get("id");
            assertEquals(id, 0);
            var userID = firstSuccess.getInt("user_id");
            assertEquals(userID, 2);

            var error = json.getJSONArray("error");
            assert (error.length() == 0);

            running.set(false);
        });

        client.start();
        {
            var json = new JSONObject();
            json.put("id", 0);
            json.put("type", RequestType.CREATE_USER_REQUEST.toLowerCaseString());
            var payload = new JSONArray();
            var request = new JSONObject();
            request.put("id", 0);
            request.put("username", "JohnDoe");
            request.put("password", "Secret Password");
            request.put("email", "john.doe@ubermail.com");
            payload.put(0, request);
            json.put("payload", payload);
            client.send(json.toString());
        }

        while (running.get()) {

        }

        client.stop();
        server.stop();

        //RunTest(server, client, running);
    }

}
