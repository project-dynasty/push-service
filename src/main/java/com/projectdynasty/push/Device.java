package com.projectdynasty.push;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Device {

    private final DeviceData data;

    public static Device get(long id) {
        DeviceData data = (DeviceData) PushService.DATABASE.getTable(DeviceData.class).query().addParameter("device_id", id).executeOne();
        if (data == null)
            return null;
        return new Device(data);
    }

    public static List<Device> getFromUser(long userId) {
        List<Device> devices = new ArrayList<>();
        List<DeviceData> datas = PushService.DATABASE.getTable(DeviceData.class).query().addParameter("user_id", userId).executeMany();
        if (datas != null && datas.size() > 0)
            datas.forEach(d -> devices.add(new Device(d)));
        return devices;
    }

    public static Device getByToken(String token) {
        DeviceData data = (DeviceData) PushService.DATABASE.getTable(DeviceData.class).query().addParameter("device_token", token).executeOne();
        if(data == null)
            return null;
        return new Device(data);
    }

    public static Device create(String address, String version, String type, String size, long userID) {
        DeviceData insert = PushService.DATABASE.getTable(DeviceData.class);
        insert.ipAddress = address;
        insert.osType = type;
        insert.osVersion = version;
        insert.screenSize = size;
        insert.user_id = userID;
        insert.deviceToken = "";
        return get(insert.insert());
    }

    public long getId() {
        return data.device_id;
    }

    public long getUserID() {
        return data.user_id;
    }

    public String getToken() {
        return data.deviceToken;
    }

    public void setToken(String token) {
        DeviceData update = PushService.DATABASE.getTable(DeviceData.class);
        update.deviceToken = token;
        data.update(update, data);
        data.deviceToken = token;
    }

    public String getIpAddress() {
        return data.ipAddress;
    }

    public String getOsType() {
        return data.osType;
    }

    public String getOsVersion() {
        return data.osVersion;
    }

    public String getScreenSize() {
        return data.screenSize;
    }

}
