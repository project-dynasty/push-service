package com.projectdynasty.push;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.util.*;
import lombok.Builder;
import org.json.JSONObject;

@Builder
public class PushNotification {
    private String message, title, deviceToken, category, sound, customName, badge;
    private boolean timeSensitive;
    private JSONObject custom;

    public static void trigger2fa(String deviceToken, String signInToken, String numbers) {
        JSONObject twoFa = new JSONObject();
        twoFa.put("token", signInToken);
        twoFa.put("numbers", numbers);
        PushNotification.builder()
                .message("Please confirm your login")
                .title("Confirm Sign in")
                .deviceToken(deviceToken)
                .sound("default")
                .timeSensitive(true)
                .customName("2fa")
                .custom(twoFa)
                .build()
                .send();
    }

    public void send() {
        send(PushService.CLIENTS.get(PushService.CONFIG.get("apns", PushService.ApnsConfig.class).isLive()));
    }

    public void send(boolean live) {
        send(PushService.CLIENTS.get(live));
    }

    public void send(ApnsClient client) {
        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertBody(message);
        if (timeSensitive) payloadBuilder.setInterruptionLevel(InterruptionLevel.TIME_SENSITIVE);
        if (title != null) payloadBuilder.setAlertTitle(title);
        if (category != null) payloadBuilder.setCategoryName(category);
        if (sound != null) payloadBuilder.setSound(sound);
        if (custom != null) payloadBuilder.addCustomProperty(customName, custom);
        if (badge != null) payloadBuilder.setBadgeNumber(Integer.parseInt(badge));

        String payload = payloadBuilder.build();
        String token = TokenUtil.sanitizeTokenString(deviceToken);
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, PushService.CONFIG.get("apns", PushService.ApnsConfig.class).getTopic(), payload);
        client.sendNotification(pushNotification);
    }

}
