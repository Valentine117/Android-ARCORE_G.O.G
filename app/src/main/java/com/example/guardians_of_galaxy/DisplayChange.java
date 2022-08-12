package com.example.guardians_of_galaxy;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class DisplayChange extends Service {
    static MediaPlayer mp;
    static boolean isStarted = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mp.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isStarted) {
            mp = MediaPlayer.create(this, R.raw.gamebgm);
            mp.setLooping(true);
            mp.start();
            mp.setVolume(0.3f, 0.3f);
            isStarted = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
