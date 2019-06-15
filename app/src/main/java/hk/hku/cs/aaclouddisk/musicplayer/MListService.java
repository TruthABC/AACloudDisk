package hk.hku.cs.aaclouddisk.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MListService extends Service {

    //Tag
    public static final String TAG = "MListService";
    public static final String DEBUG_TAG = "shijian";

    //Service Binder
    private MListService.MListServiceBinder mMListServiceBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mMListServiceBinder = new MListServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMListServiceBinder;
    }

    public class MListServiceBinder extends Binder {

    }
}
