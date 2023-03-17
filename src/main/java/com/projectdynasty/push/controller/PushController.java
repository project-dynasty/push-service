package com.projectdynasty.push.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.projectdynasty.push.Device;
import com.projectdynasty.push.PushNotification;
import com.projectdynasty.push.PushService;
import de.alexanderwodarz.code.web.StatusCode;
import de.alexanderwodarz.code.web.rest.RequestData;
import de.alexanderwodarz.code.web.rest.ResponseData;
import de.alexanderwodarz.code.web.rest.annotation.PathVariable;
import de.alexanderwodarz.code.web.rest.annotation.RequestBody;
import de.alexanderwodarz.code.web.rest.annotation.RestController;
import de.alexanderwodarz.code.web.rest.annotation.RestRequest;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;

@RestController(produces = MediaType.APPLICATION_JSON)
public class PushController {

    @RestRequest(path = "/send/user/{user}", method = "POST")
    public static ResponseData sendToUser(@PathVariable("user") String user, @RequestBody JSONObject body) {
        for (Device device : Device.getFromUser(Integer.parseInt(user))) {
            if (device.getToken() == null || device.getToken().length() == 0) continue;
            PushNotification.PushNotificationBuilder builder = sendMessage(body);
            builder = builder.deviceToken(device.getToken());
            if (body.has("live") && body.get("live") instanceof Boolean)
                builder.build().send(body.getBoolean("live"));
            else
                builder.build().send();
        }
        return new ResponseData("{}", StatusCode.OK);
    }

    @RestRequest(path = "/send/token/{token}", method = "POST")
    public static ResponseData sendToToken(@PathVariable("token") String token, @RequestBody JSONObject body) {
        Device device = Device.getByToken(token);
        if (device == null) return new ResponseData("{}", StatusCode.NOT_FOUND);
        PushNotification.PushNotificationBuilder builder = sendMessage(body);
        builder = builder.deviceToken(token);
        if (body.has("live") && body.get("live") instanceof Boolean)
            builder.build().send(body.getBoolean("live"));
        else
            builder.build().send();
        return new ResponseData("{}", StatusCode.OK);
    }

    @RestRequest(path = "/update", method = "POST")
    public static ResponseData updateToken(@RequestBody JSONObject body, RequestData data) {
        if (!body.has("token") || !(body.get("token") instanceof String) || body.getString("token").length() == 0)
            return new ResponseData("{}", StatusCode.BAD_REQUEST);
        DecodedJWT jwt = PushService.VERIFIER.verify(data.getHeader("authorization").substring(7));
        if (jwt.getClaim("deviceId").isMissing() || jwt.getClaim("deviceId").isNull() || !jwt.getClaim("mobile").asBoolean())
            return new ResponseData("{}", StatusCode.BAD_REQUEST);
        Device device = Device.get(jwt.getClaim("deviceId").asLong());
        device.setToken(body.getString("token"));
        return new ResponseData("{}", StatusCode.OK);
    }

    private static PushNotification.PushNotificationBuilder sendMessage(JSONObject object) {
        PushNotification.PushNotificationBuilder builder = PushNotification.builder();
        for (String s : object.keySet()) {
            switch (s) {
                case "title": {
                    builder = builder.title(object.getString(s));
                    break;
                }
                case "message": {
                    builder = builder.message(object.getString(s));
                    break;
                }
                case "sound": {
                    builder = builder.sound(object.getString(s));
                    break;
                }
                case "timeSensitive": {
                    builder = builder.timeSensitive(object.getBoolean(s));
                    break;
                }
                case "badge": {
                    builder = builder.badge(object.getInt(s) + "");
                    break;
                }
            }
        }
        return builder;
    }


}
