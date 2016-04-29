package com.qualcode.catchafish;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
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
import android.widget.CompoundButton;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (MatchFound(DeviceMessage.fromNearbyMessage(message).getMessageBody()))
                        {
                            Alert();
                        }
                    }
                });
            }

            @Override
            public void onLost(final Message message) {
            }
        };

        findViewById(R.id.btn_about_me).setOnClickListener(this);
        findViewById(R.id.btn_looking_for).setOnClickListener(this);

        final ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_available);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.txt_status)).setText("");
                    publish();
                    subscribe();
                } else {

                    if (mRingtone != null)
                       mRingtone.stop();

                    unpublish();
                    unsubscribe();
                    findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
                }
            }
        });
    }

    private void Alert()
    {
        final AppPreferences prefs = new AppPreferences(this);

        mRingtone = RingtoneManager.getRingtone(this, Uri.parse(prefs.getRingtone()));
        mRingtone.play();

        if (prefs.getDisableVibrate() == false)
        {
            ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);
        }

        ((TextView) findViewById(R.id.txt_status)).setText(mDisplayMsg);
        findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
    }


    private Boolean MatchFound(final String info)
    {
        final AppPreferences prefs = new AppPreferences(this);
        final HashMap<String, String> fishInfo = Utilities.convert(info);

        final StringBuilder sbMessage = new StringBuilder();

        sbMessage.append(getString(R.string.match_msg_intro));

        if (fishInfo.get("msg") != null && fishInfo.get("msg").length() > 0) {
            sbMessage.append("\n\n");
            sbMessage.append(fishInfo.get("msg"));
        }

        final Boolean lookingForMale = prefs.getLookingForMale();
        final Boolean lookingForFemale = prefs.getLookingForFemale();

        final int lookingForMinAge = prefs.getLookingForMinAge();
        final int lookingForMaxAge = prefs.getLookingForMaxAge();
        final String[] lookingForInterests = prefs.getUserInterests();
        final String[] lookingForRace = prefs.getLookingForRace();

        final Boolean fishIsMale = fishInfo.get("sex").equals("0");
        final Boolean fishIsFemale = fishInfo.get("sex").equals("1");
        final int fishAge = Integer.valueOf(fishInfo.get("age"));
        final int fishRace = Integer.valueOf(fishInfo.get("race"));
        final String[] fishInterests = fishInfo.get("interests").split(",");

        /*
        String status = "Looking For Male: " + lookingForMale + "\n\n";
        status += "Looking For Female: " + lookingForFemale + "\n\n";
        status += "Fish is Male: " + fishIsMale + "\n\n";
        status += "Fish is Female: " + fishIsFemale + "\n\n";

        ((TextView)findViewById(R.id.txt_status)).setText(status);
        */

        if (fishIsMale && lookingForMale == false || fishIsFemale && lookingForFemale == false)
            return false;

        if (fishAge >= 18 && fishAge < 100 && fishAge < lookingForMinAge || fishAge > lookingForMaxAge)
            return false;

        Boolean raceFound = false;

        if (lookingForRace != null && lookingForRace.length > 0) {
            for (final String lr : lookingForRace) {
                if (Integer.valueOf(lr).equals(fishRace)) {
                    raceFound = true;
                    break;
                }
            }
        }

        if (raceFound == false)
            return false;

        Boolean interestFound = false;

        if (lookingForInterests != null && lookingForInterests.length > 0) {
            sbMessage.append("\n\n");
            sbMessage.append(getString(R.string.match_msg_interests_header));
            sbMessage.append(" ");
            for (final String li : lookingForInterests) {
                for (final String fi : fishInterests) {
                    if (Integer.valueOf(li).equals(Integer.valueOf(fi))) {
                        interestFound = true;
                        sbMessage.append(Utilities.GetInterest(this, Integer.valueOf(fi)));
                        sbMessage.append(",");
                        sbMessage.append(" ");
                    }
                }
            }
        }

        if (interestFound == false)
            return false;

        mDisplayMsg = sbMessage.toString().replaceAll(", $", "");

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
                                //display publishing message
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
                Toast.makeText(this,
                        "No connectivity, cannot proceed. Fix in 'Settings' and try again.",
                        Toast.LENGTH_LONG).show();
                //resetToDefaultState();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText(this, "Unsuccessful: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
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

}
