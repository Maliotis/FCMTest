package com.example.fcmtest;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private boolean useFCM = true;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private HashMap<String, Object> remoteConfigDefaults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Have an option to trigger the firebase FCM
        if (useFCM) enableFirebaseFCM(); //tested and works

        //initialize defaults
        initializeRemoteConfigDefaults();
        //Experiment with remote-config
        remoteConfig();

    }

    private void initializeRemoteConfigDefaults() {
        remoteConfigDefaults = new HashMap<>();
        remoteConfigDefaults.put("holiday_promo_code", "false");
        //....put more
    }

    private void remoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Toast.makeText(MainActivity.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        holidayPromo();
                    }
                });
    }

    private void holidayPromo() {
        String welcomeMessage = mFirebaseRemoteConfig.getString("holiday_promo_code");
        if (welcomeMessage.equals("true")) {
            TextView textView = findViewById(R.id.text_view);
            textView.setTextSize(16f);
            textView.setText("Promo code enabled");
        }

    }

    private void enableFirebaseFCM() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        OnCompleteListener<InstanceIdResult> result = getListener();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(result);

    }

    private OnCompleteListener<InstanceIdResult> getListener() {
        return new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }
                // Get new Instance ID token
                //send to server the new ID token
                //sendRegistrationToServer(token);
                String token = task.getResult().getToken();
                // Log and toast
                //String msg = getString(R.string.msg_token_fmt, String.format(token));
                Log.d(TAG, "token: " + token);
                Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
