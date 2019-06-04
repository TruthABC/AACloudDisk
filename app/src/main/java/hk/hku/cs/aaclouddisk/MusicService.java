package hk.hku.cs.aaclouddisk;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class MusicService extends Service {

    public static final String TAG = "MusicService";
    public static final int ALL_CYCLE = 0;
    public static final int SINGLE_CYCLE = 1;
    public static final int ALL_RANDOM = 2;

    private MusicServiceBinder mMusicServiceBinder;
    private MediaPlayer mMediaPlayer;

    private List<String> mResourceList;
    private boolean mIsResourceReset;

    private int mNowResourceIndex;
    private boolean mHalfMusicPlayed;

    private int mPlayingMode;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mMusicServiceBinder = new MusicServiceBinder();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener((mp)->{
            jumpNextMusic();
            playNowMusicFromBeginning();
        });
        mMediaPlayer.setOnCompletionListener((mp)->{

        });
        mResourceList = null;
        mIsResourceReset = false;
        mNowResourceIndex = -1;
        mHalfMusicPlayed = false;
        mPlayingMode = ALL_CYCLE;
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

    class MusicServiceBinder extends Binder {

        public void setResourceList(List<String> resourceList) {
            Log.d("TAG", "setResourceList() executed");
            mResourceList = resourceList;
            mIsResourceReset = true;
        }

        public void playMusic() {
            Log.d("TAG", "playMusic() executed");
            //1. No or empty Resource List
            if (mResourceList == null || mResourceList.size() == 0) {
                return;
            }
            //2. New Resource List
            if (mIsResourceReset) {
                mIsResourceReset = false;
                mNowResourceIndex = -1;
                mHalfMusicPlayed = false;
            }
            //3. Continue to Play unfinished music
            if (mHalfMusicPlayed) {
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
                return;
            }
            //4. Else Default: play the music
            // if (!mHalfMusicPlayed)
            {
                jumpNextMusic();
                playNowMusicFromBeginning();
            }
        }

        public void pauseMusic() {
            Log.d("TAG", "pauseMusic() executed");
            mMediaPlayer.pause();
        }

    }

    private void jumpNextMusic() {
        mNowResourceIndex++;
        if (mNowResourceIndex >= mResourceList.size()) {
            mNowResourceIndex = 0;
        }
    }

    private void jumpPreviousMusic() {
        mNowResourceIndex--;
        if (mNowResourceIndex < 0) {
            mNowResourceIndex = mResourceList.size() - 1;
        }
    }

    private void playNowMusicFromBeginning() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        mHalfMusicPlayed = false;
        try {
            mMediaPlayer.setDataSource(mResourceList.get(mNowResourceIndex));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener((mp) -> {
                mMediaPlayer.start();
                mHalfMusicPlayed = true;
            });
        } catch (Exception e) {
            Log.i("shijian", "Exception Caught in playNowMusicFromBeginning()");
            e.printStackTrace();
            mHalfMusicPlayed = false;
        }
    }

}
