package hk.hku.cs.aaclouddisk.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import hk.hku.cs.aaclouddisk.R;

public class MusicPlayerActivity extends AppCompatActivity implements ServiceConnection {

    //Tag
    public static final String TAG = "MusicPlayerActivity";
    public static final String DEBUG_TAG = "shijian";

    //local cache
    private SharedPreferences sharedPreferences;

    //Handler
    private Handler mHandler;

    //Views
    private RelativeLayout mLeftTopButtonWrapper;
    private TextView mTitle;

    private RecyclerView mMusicListRecyclerView;
    private RecyclerView.LayoutManager mRecyclerViewManager;
    private MusicPlayerListAdaptor mRecyclerViewAdaptor;

    private TextView mMusicTimeText;
    private SeekBar mMusicSeekBar;
    private TextView mMusicEndTimeText;
    private boolean isSeeking = false;

    private RelativeLayout mModeButtonWrapper;
    private RelativeLayout mPreviousButtonWrapper;
    private RelativeLayout mPlayPauseButtonWrapper;
    private RelativeLayout mNextButtonWrapper;
    private RelativeLayout mMusicListButtonWrapper;

    //Views - Inner
    private ImageView[] mModesImageView;
    private ImageView mPlayImageView;
    private ImageView mPauseImageView;

    //Playing Music
    private MusicService.MusicServiceBinder mMusicServiceBinder;

    //for initializing mMusicServiceBinder;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mMusicServiceBinder = (MusicService.MusicServiceBinder) service;

        mRecyclerViewAdaptor.getResourceList().addAll(mMusicServiceBinder.getResourceList());
        mRecyclerViewAdaptor.notifyDataSetChanged();

        mMusicServiceBinder.setOuterOnPreparedListener((v) -> {
            showShortToast("[playing start]" + mMusicServiceBinder.getMediaPlayer().getDuration() / 1000);
            mPlayPauseButtonWrapper.setClickable(true);
            mMusicTimeText.setText("00:00");
            int maxPosition = mMusicServiceBinder.getMediaPlayer().getDuration() / 1000;
            String min = "" + maxPosition/60;
            String sec = "" + maxPosition%60;
            if (min.length() == 1)
                min = "0" + min;
            if (sec.length() == 1)
                sec = "0" + sec;
            mMusicSeekBar.setMax(maxPosition);
            mMusicSeekBar.setProgress(0);
            mMusicEndTimeText.setText(min + ":" + sec);
        });

        refreshControlBar();
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Log.v(TAG, "onCreate");

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        mHandler = new Handler();

        initViews();
        initServiceBinder();
        initEvents();
        initSeekBarSynchronization();
        initFinal();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMusicServiceBinder != null) {
            refreshControlBar();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        mMusicServiceBinder.clearOuterOnPreparedListener();
        unbindService(this);
    }

    private void initServiceBinder() {
        Intent bindIntent = new Intent(this, MusicService.class);
        bindService(bindIntent, this, BIND_AUTO_CREATE);
    }

    private void initViews() {
        mLeftTopButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_left_top_button_wrapper);
        mTitle = (TextView) findViewById(R.id.music_player_title);

        //Music Player List (Body)
        mRecyclerViewManager = new LinearLayoutManager(this);
        ((LinearLayoutManager)mRecyclerViewManager).setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewAdaptor = new MusicPlayerListAdaptor(this, new ArrayList<>());
        mMusicListRecyclerView = (RecyclerView) findViewById(R.id.music_player_list);
        mMusicListRecyclerView.setLayoutManager(mRecyclerViewManager);
        mMusicListRecyclerView.setAdapter(mRecyclerViewAdaptor);

        mMusicTimeText = (TextView) findViewById(R.id.music_player_progress_bar_time);
        mMusicSeekBar = (SeekBar) findViewById(R.id.music_player_progress_seekBar);
        mMusicEndTimeText = (TextView) findViewById(R.id.music_player_progress_bar_end_time);

        mModeButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_control_button_wrapper_mode);
        mPreviousButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_control_button_wrapper_prev);
        mPlayPauseButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_control_button_wrapper_play);
        mNextButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_control_button_wrapper_next);
        mMusicListButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_control_button_wrapper_list);

        mModesImageView = new ImageView[3];
        mModesImageView[0] = (ImageView) findViewById(R.id.music_player_button_repeat);
        mModesImageView[1] = (ImageView) findViewById(R.id.music_player_button_repeat_one);
        mModesImageView[2] = (ImageView) findViewById(R.id.music_player_button_shuffle);
        mPlayImageView = (ImageView) findViewById(R.id.music_player_button_play);
        mPauseImageView = (ImageView) findViewById(R.id.music_player_button_pause);
    }

    private void initEvents() {
        mLeftTopButtonWrapper.setOnClickListener((v) -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
        mPlayPauseButtonWrapper.setOnClickListener((v) -> {
            if (mMusicServiceBinder.getMediaPlayer().isPlaying()) {
                mMusicServiceBinder.pause();
                mPlayImageView.setVisibility(View.VISIBLE);
                mPauseImageView.setVisibility(View.INVISIBLE);
                showShortToast("[pause]");
            } else {
                mMusicServiceBinder.play();
                mPlayImageView.setVisibility(View.INVISIBLE);
                mPauseImageView.setVisibility(View.VISIBLE);
                if (!mMusicServiceBinder.isHalfMusicPlayed()) {
                    mPlayPauseButtonWrapper.setClickable(false);
                }
                showShortToast("[play]");
            }
        });
        mPreviousButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.prev();
            showShortToast("[last music]");
            mPlayImageView.setVisibility(View.INVISIBLE);
            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayPauseButtonWrapper.setClickable(false);
        });
        mNextButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.next();
            showShortToast("[next music]");
            mPlayImageView.setVisibility(View.INVISIBLE);
            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayPauseButtonWrapper.setClickable(false);
        });
        mModeButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.changePlayingMode();
            refreshControlBar();
            showShortToast("[mode changed]");
        });
        mMusicListButtonWrapper.setOnClickListener((v) -> {
            showShortToast("TODO");
        });
    }

    private void initSeekBarSynchronization() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMusicServiceBinder != null && mMusicServiceBinder.getMediaPlayer().isPlaying()) {
                    int currentPosition = mMusicServiceBinder.getMediaPlayer().getCurrentPosition() / 1000;
                    String min = "" + currentPosition/60;
                    String sec = "" + currentPosition%60;
                    if (min.length() == 1)
                        min = "0" + min;
                    if (sec.length() == 1)
                        sec = "0" + sec;
                    mMusicTimeText.setText(min + ":" + sec);
                    mMusicSeekBar.setProgress(currentPosition);
                }
                mHandler.postDelayed(this, 100);
            }
        });
    }

    private void initFinal() {

    }

    /**
     * refresh MusicPlayerActivity control bar, according to MusicService
     */
    private void refreshControlBar() {
        //Playing Mode
        for (ImageView iv: mModesImageView) {
            iv.setVisibility(View.INVISIBLE);
        }
        mModesImageView[mMusicServiceBinder.getPlayingMode()].setVisibility(View.VISIBLE);

        //Play or Pause
        if (mMusicServiceBinder.getMediaPlayer().isPlaying()) {
            mPlayImageView.setVisibility(View.INVISIBLE);
            mPauseImageView.setVisibility(View.VISIBLE);
        } else {
            mPlayImageView.setVisibility(View.VISIBLE);
            mPauseImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void showShortToast(final String msg) {
        runOnUiThread(() -> {
            Toast.makeText(MusicPlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
