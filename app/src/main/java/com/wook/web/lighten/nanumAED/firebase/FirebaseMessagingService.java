package com.wook.web.lighten.nanumAED.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.wook.web.lighten.nanumAED.activity.CPRActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.wook.web.lighten.nanumAED.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    String title = "";
    String contents = "";
    String room = "";
    int count = 0;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

            sendPushNotification(remoteMessage.getData().get("message"));
    }

    private void sendPushNotification(String message) {


            Log.d(TAG,"received message : " + message);

            if (message == null) {

            } else {
                try {


                    JSONObject jsonRootObject = new JSONObject(message);

                    title = jsonRootObject.getString("title");

                    contents = jsonRootObject.getString("body");

                    room = jsonRootObject.getString("room");


                } catch (JSONException e) {

                    e.printStackTrace();

                }


                String channelId = "VideoMessenger_01";
                String channelName = "VideoMessenger";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationManager notifManager = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            channelId, channelName, importance);

                    notifManager.createNotificationChannel(mChannel);

                }

                Notification.Builder builder = new Notification.Builder(getApplicationContext(), channelId);

                Intent intent = new Intent(this, CPRActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("roomName", room);
                if(!room.equals("") && room != null ) {
                    intent.putExtras(bundle);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                int requestID = (int) System.currentTimeMillis();

                Bitmap bitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);

                PendingIntent pendingIntent

                        = PendingIntent.getActivity(getApplicationContext()

                        , requestID

                        , intent

                        , PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentTitle(title) // required
                        .setContentText(contents)  // required
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setAutoCancel(true) //

                        .setSound(RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                        .setSmallIcon(R.drawable.icon)

                        .setLargeIcon(bitmap)
                        .setBadgeIconType(R.drawable.icon)

                        .setContentIntent(pendingIntent);

                notifManager.notify(count, builder.build());

                count++;


            }
        }


    public Bitmap getBitmapFromURL(String strURL) {

        try {

            URL url = new URL(strURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);

            connection.connect();

            InputStream input = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            return myBitmap;

        } catch (IOException e) {

            e.printStackTrace();

            return null;

        }

    }
}
