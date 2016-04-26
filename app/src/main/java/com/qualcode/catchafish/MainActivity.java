package com.qualcode.catchafish;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
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
                            ((TextView)findViewById(R.id.txt_status)).setText("Match Found!");
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
                    publish();
                    subscribe();
                } else {
                    unpublish();
                    unsubscribe();
                }
            }
        });
    }

    private Boolean MatchFound(final String fi)
    {
        final AppPreferences prefs = new AppPreferences(getApplicationContext());
        final HashMap<String, String> fishInfo = convert(fi);

        final Boolean lookingForMale = prefs.getLookingForMale();
        final Boolean lookingForFemale = prefs.getLookingForFemale();

        final int lookingForMinAge = prefs.getLookingForMinAge();
        final int lookingForMaxAge = prefs.getLookingForMaxAge();
        final String[] lookingForRace = prefs.getLookingForRace();

        final String fishName = fishInfo.get("name");
        final String fishMsg = fishInfo.get("msg");
        final Boolean fishIsMale = fishInfo.get("sex").equals("0");
        final Boolean fishIsFemale = fishInfo.get("sex").equals("1");
        final int fishAge = Integer.valueOf(fishInfo.get("age"));
        final String fishRace = fishInfo.get("race");
        final String fishInterests = fishInfo.get("interests");

        /*
        String status = "Looking For Male: " + lookingForMale + "\n\n";
        status += "Looking For Female: " + lookingForFemale + "\n\n";
        status += "Fish is Male: " + fishIsMale + "\n\n";
        status += "Fish is Female: " + fishIsFemale + "\n\n";

        ((TextView)findViewById(R.id.txt_status)).setText(status);
        */

        if (fishIsMale && lookingForMale || fishIsFemale && lookingForFemale)
        {
            if (fishAge >= 18 && fishAge > lookingForMinAge && fishAge < lookingForMaxAge)
            {
                return true;
            }
            return true;
        }

        return false;
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> convert(String str) {
        String[] tokens = str.split("&");
        HashMap<String, String> map = new HashMap<String, String>();

        final int length = tokens.length;

        for (int i = 0;i < length; i++)
        {
            String[] strings = tokens[i].split("=");

            if(strings.length == 2)
                map.put(strings[0], strings[1].replaceAll("%2C", ","));
        }

        return map;
    }
}
