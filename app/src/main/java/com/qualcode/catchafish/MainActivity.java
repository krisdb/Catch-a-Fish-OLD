package com.qualcode.catchafish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{

    private static final String TAG = "MainActivity";
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder().setTtlSeconds(3 * 60).build();
    private GoogleApiClient mGoogleApiClient;
    private Message mDeviceInfoMessage;
    private MessageListener mMessageListener;
    private boolean mResolvingNearbyPermissionError = false;
    private String mDisplayMsg;
    private Ringtone mRingtone;
    private AppPreferences mPrefs;
    private final ArrayList<String> mNearbyDevices = new ArrayList<>();
    private StringBuilder mNearbyMsg;
    RadarView mRadarView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = new AppPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SetMessage();

        mRadarView = (RadarView) findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
        mRadarView.setFrameRate(150);

        //mPrefs.setRunCount(mPrefs.getRunCount());
        ((TextView) findViewById(R.id.txt_status)).setText(getString(R.string.help_first_time));
        findViewById(R.id.btn_about_me).setOnClickListener(this);
        findViewById(R.id.btn_looking_for).setOnClickListener(this);

        final ToggleButton toggle = (ToggleButton)findViewById(R.id.btn_available);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    initSearch();
                } else {
                    stopSearch();
                }
            }
        });
    }

    private void SetMessage()
    {
        mNearbyMsg = new StringBuilder();
        mMessageListener = new MessageListener() {

            @Override
            public void onFound(final Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Match Found");
                        mNearbyDevices.clear();
                        final String msg = DeviceMessage.fromNearbyMessage(message).getMessageBody();
                        final HashMap<String, String> info = Utilities.convert(msg);
                        mNearbyDevices.add(info.get("phoneid"));
                        findViewById(R.id.txt_nearby_devices_header).setVisibility(View.VISIBLE);

                        if (mPrefs.getDisableVibrate() == false)
                        {
                            ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                        }

                        for(String device : mNearbyDevices)
                        {
                            mNearbyMsg.append(device);
                            mNearbyMsg.append(", ");
                        }

                        if (MatchFound(info)) {
                            Alert();
                            stopSearch();
                        }
                        else {
                            ((TextView)findViewById(R.id.txt_nearby_devices)).setText(mNearbyMsg.toString().replaceAll(", $", ""));
                        }
                    }
                });
            }

            @Override
            public void onLost(final Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNearbyMsg.setLength(0);
                        final String msg = DeviceMessage.fromNearbyMessage(message).getMessageBody();
                        final HashMap<String, String> info = Utilities.convert(msg);
                        mNearbyDevices.remove(info.get("phoneid"));

                        if (mNearbyDevices.size() > 0) {
                            for (String device : mNearbyDevices) {
                                mNearbyMsg.append(device);
                                mNearbyMsg.append("\n");
                            }
                            ((TextView) findViewById(R.id.txt_nearby_devices)).setText(mNearbyMsg.toString().replaceAll(", $", ""));
                        }
                        else {
                            ((TextView) findViewById(R.id.txt_nearby_devices)).setText("");
                            findViewById(R.id.txt_nearby_devices_header).setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        };
    }

    private void Alert()
    {
        MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.fish_splash);
        mPlayer.start();

        //mRingtone = RingtoneManager.getRingtone(this, Uri.parse(mPrefs.getRingtone()));
        //mRingtone.play();

        if (mPrefs.getDisableVibrate() == false)
        {
            ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);
        }

        ((TextView)findViewById(R.id.txt_status)).setText(mDisplayMsg);
        findViewById(R.id.radarView).setVisibility(View.INVISIBLE);
    }


    private Boolean MatchFound(final  HashMap<String, String> fishInfo) {
        final StringBuilder sbMessage = new StringBuilder();

        sbMessage.append(getString(R.string.match_msg_intro));

        final Boolean lookingForMale = mPrefs.getLookingForMale();
        final Boolean lookingForFemale = mPrefs.getLookingForFemale();

        final int lookingForMinAge = mPrefs.getLookingForMinAge();
        final int lookingForMaxAge = mPrefs.getLookingForMaxAge();
        final String[] lookingForInterests = mPrefs.getUserInterests();
        final String[] lookingForRace = mPrefs.getLookingForRace();

        final Boolean fishIsMale = fishInfo.get("sex").equals("0");
        final Boolean fishIsFemale = fishInfo.get("sex").equals("1");
        final int fishAge = Integer.valueOf(fishInfo.get("age"));
        final Boolean hideAge = fishInfo.get("hideage").equals("true");
        final int fishRace = Integer.valueOf(fishInfo.get("race"));
        final String[] fishInterests = (fishInfo.get("interests") != null) ? fishInfo.get("interests").split(",") : new String[]{};

        Log.i(TAG, "Looking For Male: " + lookingForMale);
        Log.i(TAG, "Looking For Female: " + lookingForFemale);
        Log.i(TAG, "Fish is Male: " + fishIsMale);
        Log.i(TAG, "Fish is Female: " + fishIsFemale);
        Log.i(TAG, "Fish is Race: " + fishRace);

        sbMessage.append("\n\n");

        if (lookingForRace != null && lookingForRace.length > 0) {
            Boolean raceFound = false;
            for (final String lr : lookingForRace) {
                Log.i(TAG, "Looking for Race: " + lr);
                if (Integer.valueOf(lr).equals(fishRace)) {
                    raceFound = true;
                    break;
                }
            }

            if (raceFound == false)
                return false;

            sbMessage.append(Utilities.GetRace(this, fishRace));
            sbMessage.append(" ");
        }

        if ((lookingForMale || lookingForFemale) && (fishIsMale && lookingForMale == false || fishIsFemale && lookingForFemale == false))
            return false;
        else {
            if (fishIsMale)
                sbMessage.append("Male ");
            else if (fishIsFemale)
                sbMessage.append("Female ");
        }

        if (fishAge >= 18 && fishAge < 100 && fishAge < lookingForMinAge || fishAge > lookingForMaxAge)
            return false;
        else if (hideAge == false) {
            sbMessage.append(" Age ");
            sbMessage.append(fishAge);
        }

        if (lookingForInterests != null && lookingForInterests.length > 0) {
            Log.i(TAG, "Interests");
            sbMessage.append("\n\n");
            sbMessage.append(getString(R.string.match_msg_interests_similar_header));
            sbMessage.append("\n");

            for (final String li : lookingForInterests) {
                for (final String fi : fishInterests) {
                    if (Integer.valueOf(li).equals(Integer.valueOf(fi))) {
                        sbMessage.append(Utilities.GetInterest(this, Integer.valueOf(fi)));
                        sbMessage.append("\n");
                    }
                }
            }
        }

        if (fishInfo.get("msg") != null && fishInfo.get("msg").length() > 0) {
            sbMessage.append("\n");
            sbMessage.append("\"");
            sbMessage.append(fishInfo.get("msg"));
            sbMessage.append("\"");
        }

        mDisplayMsg = sbMessage.toString();

        (((ToggleButton) findViewById(R.id.btn_available))).setChecked(false);

        return true;
    }

    private void publish() {

        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            mDeviceInfoMessage = DeviceMessage.newNearbyMessage(getApplicationContext(), InstanceID.getInstance(getApplicationContext()).getId());

            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                        }
                    }).build();

            Nearby.Messages.publish(mGoogleApiClient, mDeviceInfoMessage, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "publish successfully");
                            } else {
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    private void subscribe() {

        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            mNearbyDevices.clear();
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            Log.i(TAG, "no longer subscribing");
                        }
                    }).build();

            Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "subscribed successfully");
                            } else {
                                Log.i(TAG, "could not subscribe");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    private void unpublish() {
        Nearby.Messages.unpublish(mGoogleApiClient, mDeviceInfoMessage);
    }

    private void unsubscribe() {
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mDeviceInfoMessage != null)
            Nearby.Messages.unpublish(mGoogleApiClient, mDeviceInfoMessage);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            SetMessage();
            initSearch();
        }
        else
        {
            stopSearch();
        }
    }

    private void handleUnsuccessfulNearbyResult(Status status) {
        Log.i(TAG, "processing error, status = " + status);
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyPermissionError) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(this, Constants.REQUEST_RESOLVE_ERROR);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(this, "No connectivity, cannot proceed. Fix in 'Settings' and try again.", Toast.LENGTH_LONG).show();
                stopSearch();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText(this, "Unsuccessful: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "connection to GoogleApiClient failed");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended: " + cause);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_about_me:
                startActivity(new Intent(MainActivity.this, SettingsAboutMe.class));
                break;
            case R.id.btn_looking_for:
                startActivity(new Intent(MainActivity.this, SettingsLookingFor.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initSearch()
    {
        mNearbyDevices.clear();
        mNearbyMsg.setLength(0);
        ((TextView) findViewById(R.id.txt_nearby_devices)).setText("");
        findViewById(R.id.txt_nearby_devices_header).setVisibility(View.INVISIBLE);
        (((ToggleButton)findViewById(R.id.btn_available))).setChecked(true);
        if (mRadarView != null) mRadarView.startAnimation();
        findViewById(R.id.radarView).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.txt_status)).setText("");
        publish();
        subscribe();
    }

    private void stopSearch()
    {
        mNearbyDevices.clear();
        mNearbyMsg.setLength(0);
        ((TextView) findViewById(R.id.txt_nearby_devices)).setText("");
        findViewById(R.id.txt_nearby_devices_header).setVisibility(View.INVISIBLE);
        (((ToggleButton)findViewById(R.id.btn_available))).setChecked(false);
        findViewById(R.id.radarView).setVisibility(View.INVISIBLE);
        if (mRadarView != null) mRadarView.stopAnimation();
        unpublish();
        unsubscribe();
        if (mRingtone != null)
            mRingtone.stop();
    }
}
