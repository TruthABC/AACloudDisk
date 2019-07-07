package hk.hku.cs.aaclouddisk.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import hk.hku.cs.aaclouddisk.GlobalTool;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicService extends Service {

    //Tag
    public static final String TAG = "MusicService";
    public static final String DEBUG_TAG = "shijian";

    //Math util
    private Random mRandom;

    //Service Binder
    private MusicServiceBinder mMusicServiceBinder;

    //Listener For Outer Event
    private MediaPlayer.OnPreparedListener mOuterOnPreparedListener;
    private MediaPlayer.OnBufferingUpdateListener mOuterOnBufferingUpdateListener;

    //For MediaPlayer
    private List<ResourceInfo> mResourceList;
    private int mNowResourceIndex;
    private List<Integer> mHistoryResourceIndex;
    private int mLastResourceIndex;
    private boolean mHalfMusicPlayed;
    private int mPlayingMode;
    private int mBufferingPercent;
    private MediaPlayer mMediaPlayer;
    private HttpProxyCacheServer proxy = null;
    //Const for mPlayingMode
    public static final int ALL_CYCLE = 0;
    public static final int SINGLE_CYCLE = 1;
    public static final int ALL_RANDOM = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mRandom = new Random();
        mRandom.setSeed(new Date().getTime());
        mMusicServiceBinder = new MusicServiceBinder();
        mOuterOnPreparedListener = null;
        mOuterOnBufferingUpdateListener = null;

        mResourceList = null;
        mNowResourceIndex = -1;
        mHistoryResourceIndex = new ArrayList<>();
        mLastResourceIndex = -1;
        mHalfMusicPlayed = false;
        mPlayingMode = ALL_CYCLE;
        mBufferingPercent = 0;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener((mp) -> {
            Log.i(TAG, "OnPrepared");
            mMediaPlayer.start();
            mHalfMusicPlayed = true;
            if (mOuterOnPreparedListener != null) {
                mOuterOnPreparedListener.onPrepared(mp);
            }
        });
        mMediaPlayer.setOnCompletionListener((mp)->{
            Log.i(TAG, "OnCompletion");
            switch (mPlayingMode) {
                case ALL_CYCLE: jumpNextMusic(); playNowMusicFromBeginning(); break;
                case SINGLE_CYCLE:  break; //Do nothing
                case ALL_RANDOM: jumpRandomMusic(); //NO break;
                default: playNowMusicFromBeginning();
            }
        });
        mMediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            if (getProxy().isCached(mResourceList.get(mNowResourceIndex).getOnlineUrl())) {
                mBufferingPercent = 100;
            } else {
                mBufferingPercent = percent;
            }
            if (mOuterOnBufferingUpdateListener != null) {
                if (getProxy().isCached(mResourceList.get(mNowResourceIndex).getOnlineUrl())) {
                    mOuterOnBufferingUpdateListener.onBufferingUpdate(mp, 100);
                } else {
                    mOuterOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
                }
            }
        });
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
        if (proxy != null) {
            proxy.shutdown();
            proxy = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicServiceBinder;
    }

    public class MusicServiceBinder extends Binder {

        public void setOuterOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
            mOuterOnPreparedListener = onPreparedListener;
        }

        public void setOuterOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
            mOuterOnBufferingUpdateListener = onBufferingUpdateListener;
        }

        public int getNowResourceIndex() {
            return mNowResourceIndex;
        }

        public List<Integer> getHistoryResourceIndex() {
            return mHistoryResourceIndex;
        }

        public int getLastResourceIndex() {
            return mLastResourceIndex;
        }

        public List<ResourceInfo> getResourceList() {
            Log.d("TAG", "getResourceList() executed");
            return mResourceList;
        }

        public void setResourceList(List<ResourceInfo> resourceList) {
            Log.d("TAG", "setResourceList() executed");
            mResourceList = resourceList;
            mNowResourceIndex = 0;
            mHistoryResourceIndex = new ArrayList<>();
            mLastResourceIndex = -1;
            mHalfMusicPlayed = false;
            mBufferingPercent = 0;
        }

        public boolean isHalfMusicPlayed() {
            return mHalfMusicPlayed;
        }

        public int getBufferingPercent () {
            return mBufferingPercent;
        }

        public int getPlayingMode() {
            return mPlayingMode;
        }

        public void changePlayingMode() {
            mPlayingMode++;
            if (mPlayingMode > 2) {
                mPlayingMode = 0;
            }
            if (mPlayingMode == SINGLE_CYCLE) {
                mMediaPlayer.setLooping(true);
            } else {
                mMediaPlayer.setLooping(false);
            }
        }

        public MediaPlayer getMediaPlayer() {
            return mMediaPlayer;
        }

        public void play() {
            Log.d("TAG", "play() executed");
            //1. No or empty Resource List
            if (mResourceList == null || mResourceList.size() == 0) {
                Log.i(TAG, "play() 1. No or empty Resource List");
                return;
            }
            //2. Continue to Play unfinished music
            if (mHalfMusicPlayed) {
                Log.i(TAG, "play() 2. Continue to Play unfinished music");
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
                return;
            }
            //3. Default: play the music
            // if (!mHalfMusicPlayed)
            {
                Log.i(TAG, "play() 3. Default: play the music");
                playNowMusicFromBeginning();
            }
        }

        public void pause() {
            Log.d("TAG", "pause() executed");
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }

        public void next() {
            Log.d("TAG", "next() executed");
            //1. No or empty Resource List
            if (mResourceList == null || mResourceList.size() == 0) {
                Log.i(TAG, "next() 1. No or empty Resource List");
                return;
            }
            //2. Default: jump next music
            // else
            {
                Log.i(TAG, "next() 2. Default: play the next music from beginning");
                if (mPlayingMode == ALL_RANDOM) {
                    jumpRandomMusic();
                } else {
                    jumpNextMusic();
                }
                playNowMusicFromBeginning();
            }
        }

        public void prev() {
            Log.d("TAG", "prev() executed");
            //1. No or empty Resource List
            if (mResourceList == null || mResourceList.size() == 0) {
                Log.i(TAG, "prev() 1. No or empty Resource List");
                return;
            }
            //2. Default: jump prev music
            // else
            {
                Log.i(TAG, "prev() 2. Default: play the previous music from beginning");
                if (mPlayingMode == ALL_RANDOM) {// ALL_RANDOM::prev means go back to last played one
                    int size = mHistoryResourceIndex.size();
                    if (size == 0) {// ALL_RANDOM::prev, if no more history played ones
                        jumpRandomMusic();
                    } else {
                        boolean jumpSuccessful = jumpTo(mHistoryResourceIndex.get(size-1));
                        if (jumpSuccessful) {// this will make actual size++
                            mHistoryResourceIndex.remove(size);
                        }
                        mHistoryResourceIndex.remove(size-1);
                    }
                } else {
                    jumpPreviousMusic();
                }
                playNowMusicFromBeginning();
            }
        }

        public boolean jumpTo(int newIndex) {
            boolean jumpSuccess = jumpToMusic(newIndex);
            if (jumpSuccess || newIndex == mNowResourceIndex) {
                playNowMusicFromBeginning();
            }
            return jumpSuccess;
        }

        public HttpProxyCacheServer getProxy() {
            return MusicService.this.getProxy();
        }
    }

    private void jumpNextMusic() {
        mHistoryResourceIndex.add(mNowResourceIndex);
        mLastResourceIndex = mNowResourceIndex;
        mNowResourceIndex++;
        if (mNowResourceIndex >= mResourceList.size()) {
            mNowResourceIndex = 0;
        }
    }

    private void jumpPreviousMusic() {
        mHistoryResourceIndex.add(mNowResourceIndex);
        mLastResourceIndex = mNowResourceIndex;
        mNowResourceIndex--;
        if (mNowResourceIndex < 0) {
            mNowResourceIndex = mResourceList.size() - 1;
        }
    }

    private void jumpRandomMusic() {
        mHistoryResourceIndex.add(mNowResourceIndex);
        mLastResourceIndex = mNowResourceIndex;
        while (mNowResourceIndex == mLastResourceIndex) {
            mNowResourceIndex = mRandom.nextInt(mResourceList.size());
        }
    }

    private boolean jumpToMusic(int newIndex) {
        //boundary
        if (mResourceList == null) {
            return false;
        }
        if (newIndex < 0 || newIndex >= mResourceList.size()) {
            return false;
        }
        //should not be the same index
        if (newIndex == mNowResourceIndex) {
            return false;
        }
        //jump OK
        mHistoryResourceIndex.add(mNowResourceIndex);
        mLastResourceIndex = mNowResourceIndex;
        mNowResourceIndex = newIndex;
        return true;
    }

    private void playNowMusicFromBeginning() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        if (mPlayingMode == SINGLE_CYCLE) {
            mMediaPlayer.setLooping(true);
        } else {
            mMediaPlayer.setLooping(false);
        }
        mHalfMusicPlayed = false;
        mBufferingPercent = 0;
        try {
            HttpProxyCacheServer proxy = getProxy();
            boolean isCached = proxy.isCached(mResourceList.get(mNowResourceIndex).getOnlineUrl());
            if (isCached) {
                mBufferingPercent = 100;
            }
            Log.i(TAG, "isCached(" + mResourceList.get(mNowResourceIndex).getName() + ")=" + isCached);
            String proxyUrl = proxy.getProxyUrl(mResourceList.get(mNowResourceIndex).getOnlineUrl());
            mMediaPlayer.setDataSource(proxyUrl);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            Log.i(TAG, "Doing prepareAsync()");
        } catch (Exception e) {
            e.printStackTrace();
            mHalfMusicPlayed = false;
            mBufferingPercent = 0;
            Log.i(TAG, "Exception Caught in playNowMusicFromBeginning()");
        }
    }

    private synchronized HttpProxyCacheServer getProxy() {
        if (proxy == null) {
            File cacheRoot = GlobalTool.getIndividualCacheDirectory(this);
            Log.i(DEBUG_TAG, "Cache Root: " + cacheRoot.getAbsolutePath());
            proxy = new HttpProxyCacheServer(this);
        }
        return proxy;
    }

}
