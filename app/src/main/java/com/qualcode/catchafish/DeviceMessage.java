package com.qualcode.catchafish;

import android.content.Context;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

public class DeviceMessage {
    private static final Gson gson = new Gson();

    private final String mInstanceId;
    private final String mMessageBody;
    private static Context mContext;

    public static Message newNearbyMessage(final Context ctx, final String instanceId) {
        mContext = ctx;
        final DeviceMessage deviceMessage = new DeviceMessage(instanceId);

        return new Message(gson.toJson(deviceMessage).toString().getBytes(Charset.forName("UTF-8")));
    }

    public static DeviceMessage fromNearbyMessage(final Message message) {
        final String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson((new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),DeviceMessage.class);
    }

    private DeviceMessage(final String instanceId) {
        this.mInstanceId = instanceId;

        final AppPreferences prefs = new AppPreferences(mContext);

        final StringBuilder message = new StringBuilder();
        message.append("msg=");
        message.append(prefs.getDisplayMsg());
        message.append("&sex=");
        message.append(prefs.getUserSex());
        message.append("&age=");
        message.append(prefs.getUserAge());
        message.append("&race=");
        message.append(prefs.getUserRace());
        message.append("&interests=");

        for(final String i : prefs.getUserInterests())
        {
            message.append(i);
            message.append(",");
        }

        this.mMessageBody = message.toString().replaceAll(", $", "");
    }

    protected String getMessageBody() {
        return mMessageBody;
    }
}