package com.app.garini.garini.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.app.garini.garini.MapsFrag;
import com.app.garini.garini.utile.UserSessionManager;
/**
 * Created by m.lagha on 21/03/2017.
 */

public class CleanService extends Service {

    UserSessionManager userSessionManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("CleanService", "onStarCommand(): Received id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        userSessionManager = new UserSessionManager(getApplicationContext());
        if(userSessionManager.isTrouver()){
            Log.i("CleanService", " id trouver " + userSessionManager.getIdTrouver());
            userSessionManager.deleteTrouver();
            if(userSessionManager.isAttribuer()){
                userSessionManager.deleteAttribuer();
            }
        }
        if(userSessionManager.isDonner() && MapsFrag.now){
            Log.i("CleanService", " id donner " + userSessionManager.getIdDonner());
            userSessionManager.deleteDonner();
            if(userSessionManager.isAttribuer()){
                userSessionManager.deleteAttribuer();
            }
        }
        startService(new Intent(this, CleanServiceNet.class));
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
