package com.example.fcmtest.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.fcmtest.MainActivity;
import com.example.fcmtest.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import org.json.JSONException;
import java.util.Map;
import java.util.Random;

/**
 * Created by petrosmaliotis on 2019-10-08.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    public static final String CHANNEL_ID = "Test channel";

    //Vars to be driven...
    private String title;
    private String body;
    private Map<String ,String> metaData;
    private String from;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d("TAG", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        from  = remoteMessage.getFrom();
        Log.d("TAG", "From: " + from);

        //If we have meta data access them
        getMetaData(remoteMessage);

        // Check if message contains a Notification payload.
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();

            //Note: both priority and importance need to be HIGH to display Head-Up-Notification
            createNotificationChannel();
            NotificationCompat.Builder builder = getNotificationBuilder();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // notificationId is a unique int for each notification that you must define
            //use the same to update the current notification
            int notificationId = new Random().nextInt();
            notificationManager.notify(notificationId, builder.build());


            //The WorkManager API makes it easy to schedule deferrable,
            // asynchronous tasks that are expected to run even if the app exits or device restarts.

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

    }

    /**
     * the Map is type of String key,vale nested objects will be flattened to Strings
     * @param remoteMessage remoteMessage contains all the info sent from the server
     */
    private void getMetaData(@NonNull RemoteMessage remoteMessage) {
        if (!remoteMessage.getData().isEmpty()) {
            metaData = remoteMessage.getData();

            Gson gson = new Gson();
            String json = gson.toJson(metaData);
            String object1 = metaData.get("field1");
            Log.d(TAG, "onMessageReceived: object field 1 = " + object1);
            Log.d(TAG, "onMessageReceived: metaData = " + metaData);
        }
    }

    /**
     * Vars to be driven by metadata
     *      - Small/Large icon
     *      - defaults
     *      - visibility (for lock screen)
     *      - vibration pattern
     *      - priority
     *      e.t.c
     * @return Notification.Builder
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        Intent fullScreenIntent = new Intent(this, MainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                //.setCategory(Notification.CATEGORY_STATUS)
                // Start without a delay
                // Vibrate for 100 milliseconds
                // Sleep for 60 milliseconds
                .setVibrate(new long[]{0, 100, 60, 100})
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    /**
     * For devices using API 26+
     * NOTE: without a channel the notification won't be triggered
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel id";
            String description = "channel id";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //channel.setShowBadge(true);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
