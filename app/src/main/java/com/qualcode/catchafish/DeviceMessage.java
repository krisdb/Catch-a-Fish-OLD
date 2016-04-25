package com.qualcode.catchafish;

import android.content.Context;
import android.os.Build;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Used to prepare the payload for a
 * {@link com.google.android.gms.nearby.messages.Message Nearby Message}. Adds a unique id (an
 * InstanceID) to the Message payload, which helps Nearby distinguish between multiple devices with
 * the same model name.
 */
public class DeviceMessage {
    private static final Gson gson = new Gson();

    private final String mInstanceId;
    private final String mMessageBody;
    private static Context mContext;

    public static Message newNearbyMessage(Context ctx, String instanceId) {
        mContext = ctx;
        DeviceMessage deviceMessage = new DeviceMessage(instanceId);

        return new Message(gson.toJson(deviceMessage).toString().getBytes(Charset.forName("UTF-8")));
    }

    public static DeviceMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson((new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),DeviceMessage.class);
    }

    private DeviceMessage(String instanceId) {
        this.mInstanceId = instanceId;

        final AppPreferences prefs = new AppPreferences(mContext);

        final StringBuilder message = new StringBuilder();
        message.append("msg=");
        message.append(prefs.getUserMsg());
        message.append("&name=");
        message.append(prefs.getUserName());
        message.append("&sex=");
        message.append(prefs.getUserSex());
        message.append("&age=");
        message.append(prefs.getUserAge());
        message.append("&race=");
        message.append(prefs.getUserRace());
        message.append("&interests=");
        message.append(prefs.getUserInterests());

        this.mMessageBody = message.toString();
    }

    protected String getMessageBody() {
        return mMessageBody;
    }
}