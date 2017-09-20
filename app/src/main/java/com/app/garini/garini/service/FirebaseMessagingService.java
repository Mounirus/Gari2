package com.app.garini.garini.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.app.garini.garini.MainActivity;
import com.app.garini.garini.login.LoginActivity;
import com.app.garini.garini.R;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by m.lagha on 29/01/2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        showNotification(remoteMessage.getData().get("message"),remoteMessage.getData().get("test"));
    }

    private void showNotification(String message,String test){
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("test", test);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Bitmap imgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_camera);

        PendingIntent pendingIntent = PendingIntent.getActivities(this,0, new Intent[]{i},PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                /*.setLargeIcon(imgBitmap)*/
                .setAutoCancel(true)
                .setContentTitle("Garini test")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());
    }
}
