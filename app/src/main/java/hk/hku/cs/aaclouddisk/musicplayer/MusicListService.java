package hk.hku.cs.aaclouddisk.musicplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import hk.hku.cs.aaclouddisk.GlobalTool;
import hk.hku.cs.aaclouddisk.entity.musicplayer.MusicList;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicListService extends Service {

    //Tag
    public static final String TAG = "MusicListService";
    public static final String DEBUG_TAG = "shijian";

    //local cache
    private SharedPreferences sharedPreferences;
    private String userId;

    //Data of MusicLists
    private Gson gson;
    private List<MusicList> mMusicLists;

    //File and Dir
    private File mUserLocalDir;
    private File mMusicListsFile;

    //Service Binder
    private MusicListServiceBinder mMusicListServiceBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("id", "");

        gson = new Gson();
        mMusicLists = new ArrayList<>();

        //try read local music list cache
        if (userId == null || userId.length()==0) {
            Log.e(DEBUG_TAG, "Should not be here. id not got, after sharedPreferences.getString(\"id\", \"\");");
        }
        mUserLocalDir = GlobalTool.getChildCacheDirectory(this, userId);
        if (!mUserLocalDir.isDirectory()) {
            mUserLocalDir.delete();
        }
        if (!mUserLocalDir.exists()) {
            mUserLocalDir.mkdirs();
        }
        mMusicListsFile = new File(mUserLocalDir.getAbsolutePath() + File.pathSeparator + "music_lists.json");
        if (mMusicListsFile.exists()) {
            Scanner scanner = GlobalTool.getFileScanner(mMusicListsFile.getAbsolutePath());
            try {
                String jsonArrayStr = "";
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    jsonArrayStr += line;
                }
                Type type = new TypeToken<ArrayList<MusicList>>(){}.getType();
                mMusicLists = gson.fromJson(jsonArrayStr, type);
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        //Service Binder
        mMusicListServiceBinder = new MusicListServiceBinder();
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
        mMusicListServiceBinder.saveMusicLists();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicListServiceBinder;
    }

    public class MusicListServiceBinder extends Binder {

        public List<MusicList> getMusicLists() {
            return mMusicLists;
        }

        public void saveMusicLists() {
            Log.i(TAG, "saveMusicLists to " + mMusicListsFile.getAbsolutePath());
            Thread thread = new Thread(()->{
                PrintStream out = GlobalTool.getFilePrintStream(mMusicListsFile.getAbsolutePath());
                try {
                    String outStr = gson.toJson(mMusicLists);
                    out.print(outStr);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (out != null) {
                        out.close();
                    }
                }
            });
            thread.start();
        }

        public void initMusicLists(List<ResourceInfo> mainActivityTempResourceList) {
            Log.i(TAG, "initMusicLists");
            if (userId == null) {
                userId = "";
            }
            MusicList musicList = new MusicList(userId, "Online-All", false, new Date().getTime(), mainActivityTempResourceList, false);
            mMusicLists.clear();
            mMusicLists.add(musicList);
        }

        public void updateOnlineList(List<ResourceInfo> mainActivityTempResourceList) {
            Log.i(TAG, "updateOnlineList");
            if (userId == null) {
                userId = "";
            }
            MusicList musicList = new MusicList(userId, "Online-All", false, new Date().getTime(), mainActivityTempResourceList, false);
            mMusicLists.set(0, musicList);
        }
    }
}
