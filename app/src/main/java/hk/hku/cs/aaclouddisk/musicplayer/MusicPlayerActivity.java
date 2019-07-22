package hk.hku.cs.aaclouddisk.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import hk.hku.cs.aaclouddisk.GlobalTool;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

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

    private ListView mPlayerBodyListView;
    public MusicPlayerBodyListAdaptor mPlayerBodyListAdaptor;
    public int mMusicListIndex;//TODO: make it in musicService

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

    //Views - Bottom Sheet
    private boolean shownBottom = false;
    private RelativeLayout mBottomSheet;
    private BottomSheetBehavior<RelativeLayout> mBottomSheetBehavior;
    private RelativeLayout mHideBottomSheetButtonWrapper;
    private RelativeLayout mCreateMusiListButtonWrapper;

    //Views - Bottom Sheet Lists
    private ListView mBottomListView;
    public MusicPlayerBottomListAdaptor mPlayerBottomListAdaptor;

    //Playing Music
    public MusicService.MusicServiceBinder mMusicServiceBinder;
    public MusicListService.MusicListServiceBinder mMusicListServiceBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Log.v(TAG, "onCreate");

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        mHandler = new Handler();

        initViews();
        initAllServiceBinder();
        initBottomSheet();
        initEvents();
        initSeekBarSynchronization();
        initFinal();
    }

    private void initViews() {
        //Title Bar
        mLeftTopButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_left_top_button_wrapper);
        mTitle = (TextView) findViewById(R.id.music_player_title);

        //Body (Music Player List)
        mPlayerBodyListAdaptor = new MusicPlayerBodyListAdaptor(this, R.layout.activity_music_player_body_item, this);
        mPlayerBodyListView = (ListView) findViewById(R.id.music_player_list);
        mPlayerBodyListView.setAdapter(mPlayerBodyListAdaptor);

        //Progress Bar (Seek Bar & Text)
        mMusicTimeText = (TextView) findViewById(R.id.music_player_progress_bar_time);
        mMusicSeekBar = (SeekBar) findViewById(R.id.music_player_progress_seekBar);
        mMusicEndTimeText = (TextView) findViewById(R.id.music_player_progress_bar_end_time);

        //Control Bar
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

        //Bottom Sheet
        mBottomSheet = (RelativeLayout) findViewById(R.id.music_player_bottom_sheet);

        //Bottom Sheet Header (topBar, not "Peek Height" part)
        mHideBottomSheetButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_bottom_right_top_button_wrapper);
        mCreateMusiListButtonWrapper = (RelativeLayout) findViewById(R.id.music_player_bottom_left_top_button_wrapper);

        //Music List lists
        mPlayerBottomListAdaptor = new MusicPlayerBottomListAdaptor(this, R.layout.activity_music_player_bottom_item, this);
        mBottomListView = (ListView) findViewById(R.id.music_player_bottom_list_view);
        mBottomListView.setAdapter(mPlayerBottomListAdaptor);
    }

    private void initAllServiceBinder() {
        //bind MusicService
        Intent bindIntent = new Intent(this, MusicService.class);
        bindService(bindIntent, this, BIND_AUTO_CREATE);
        //then bind MusicListService
        Intent bindIntent2 = new Intent(this, MusicListService.class);
        bindService(bindIntent2, this, BIND_AUTO_CREATE);
    }

    private void initBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    if (shownBottom) {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        mBottomSheetBehavior.setFitToContents(false);
        mBottomSheetBehavior.setHideable(false);//prevents the bottom sheet from completely hiding off the screen
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);//initially state
        shownBottom = false;
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
                showShortToast("Pause");
            } else {
                mMusicServiceBinder.play();
                mPlayImageView.setVisibility(View.INVISIBLE);
                mPauseImageView.setVisibility(View.VISIBLE);
                if (!mMusicServiceBinder.isHalfMusicPlayed()) {
                    mPlayPauseButtonWrapper.setClickable(false);
                }
                showShortToast("Start");
            }
        });
        mPreviousButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.prev();
            showShortToast("Prev");
            mPlayPauseButtonWrapper.setClickable(false);
        });
        mNextButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.next();
            showShortToast("Next");
            mPlayPauseButtonWrapper.setClickable(false);
        });
        mModeButtonWrapper.setOnClickListener((v) -> {
            mMusicServiceBinder.changePlayingMode();
            refreshControlBar();
            switch (mMusicServiceBinder.getPlayingMode()) {
                case MusicService.ALL_CYCLE: showShortToast("All Cycle"); break;
                case MusicService.SINGLE_CYCLE: showShortToast("Single Cycle"); break;
                case MusicService.ALL_RANDOM: showShortToast("All Random"); break;
            }

        });

        //Bottom Sheet Events
        mMusicListButtonWrapper.setOnClickListener((v) -> {
            if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                shownBottom = false;
            } else {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                shownBottom = true;
            }
        });
        mHideBottomSheetButtonWrapper.setOnClickListener((v) -> {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            shownBottom = false;
        });
        mCreateMusiListButtonWrapper.setOnClickListener((v) -> {
            showInputNewListName();
        });
    }

    private void initSeekBarSynchronization() {
        //when seek bar pushing forward or dragged
        mMusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final String progressInText = GlobalTool.secondToMinSecText(progress);
                runOnUiThread(() -> {
                    mMusicTimeText.setText(progressInText);
                });
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                int progress = seekBar.getProgress();
                if(mMusicServiceBinder != null && mMusicServiceBinder.isHalfMusicPlayed() && progress >= 0 && progress <= seekBar.getMax()){
                    mMusicServiceBinder.getMediaPlayer().seekTo(progress * 1000);
                }
            }
        });
        //every 100ms, try update seek bar's progress
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Warning: when music not prepared (not HalfPlayed), cause onCompletion (and go next music)
                if (!isSeeking && mMusicServiceBinder != null && mMusicServiceBinder.isHalfMusicPlayed() && mMusicServiceBinder.getMediaPlayer() != null) {
                    mMusicSeekBar.setProgress(mMusicServiceBinder.getMediaPlayer().getCurrentPosition() / 1000);
                }
                mHandler.postDelayed(this, 100);
            }
        });
    }

    private void initFinal() {}

    //for initializing mMusicServiceBinder and UI
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof MusicService.MusicServiceBinder) { // MusicService Ready
            mMusicServiceBinder = (MusicService.MusicServiceBinder) service;

            //Title
            int index = mMusicServiceBinder.getNowResourceIndex();
            List<ResourceInfo> resourceList = mMusicServiceBinder.getResourceList();
            if (index >= 0 && index < resourceList.size()) {
                mTitle.setText(resourceList.get(index).getName());
            }

            //Body (Music List as Body)
            mMusicListIndex = 0;
            mPlayerBodyListAdaptor.addAll(mMusicServiceBinder.getResourceList());
            mPlayerBodyListAdaptor.notifyDataSetChanged();
            mPlayerBodyListView.smoothScrollToPosition(index);
            refreshMusicListHighlight();

            //Progress Bar & call back
            refreshMusicProgressMaxSecond();
            refreshMusicBufferPercent();
            mMusicServiceBinder.setOuterOnPreparedListener((v) -> {
                mTitle.setText(mMusicServiceBinder.getResourceList().get(mMusicServiceBinder.getNowResourceIndex()).getName());
                mPlayImageView.setVisibility(View.INVISIBLE);
                mPauseImageView.setVisibility(View.VISIBLE);
                mPlayPauseButtonWrapper.setClickable(true);
                refreshMusicListHighlight();
                refreshMusicProgressMaxSecond();
                refreshMusicBufferPercent();
            });
            mMusicServiceBinder.setOuterOnBufferingUpdateListener((mp, percent) -> {
                runOnUiThread(() -> {
                    mMusicSeekBar.setSecondaryProgress((percent * mMusicSeekBar.getMax()) / 100);
                });
            });

            //Control Bar
            refreshControlBar();

            //Bottom List
            mPlayerBottomListAdaptor.setMusicServiceBinder(mMusicServiceBinder);
        } else if (service instanceof MusicListService.MusicListServiceBinder) { // ListService Ready
            mMusicListServiceBinder = (MusicListService.MusicListServiceBinder) service;
            mPlayerBottomListAdaptor.setMusicListServiceBinder(mMusicListServiceBinder);
            mPlayerBottomListAdaptor.addAll(mMusicListServiceBinder.getMusicLists());
            mPlayerBottomListAdaptor.notifyDataSetChanged();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        mMusicServiceBinder.setOuterOnPreparedListener(null);
        mMusicServiceBinder.setOuterOnBufferingUpdateListener(null);
        unbindService(this);
    }

    /**
     * refresh Body (MusicList) Highlight (playing) Item
     */
    private void refreshMusicListHighlight() {
        //Highlight NowPlaying
        int index = mMusicServiceBinder.getNowResourceIndex();
        index = index - mPlayerBodyListView.getFirstVisiblePosition();
        RelativeLayout rootItem = (RelativeLayout) mPlayerBodyListView.getChildAt(index);
        if (rootItem != null) {
            rootItem.findViewById(R.id.front_image).setVisibility(View.VISIBLE);
            ((TextView)rootItem.findViewById(R.id.resource_name)).setTextColor(getResources().getColor(R.color.primary_light));
        }
        //De-Highlight LastPlaying
        index = mMusicServiceBinder.getLastResourceIndex();
        index = index - mPlayerBodyListView.getFirstVisiblePosition();
        rootItem = (RelativeLayout) mPlayerBodyListView.getChildAt(index);
        if (rootItem != null) {
            rootItem.findViewById(R.id.front_image).setVisibility(View.GONE);
            ((TextView)rootItem.findViewById(R.id.resource_name)).setTextColor(getResources().getColor(R.color.white_c));
        }
    }

    /**
     * refresh Seek Bar Secondary Progress of music Buffer percent
     */
    private void refreshMusicBufferPercent() {
        mMusicSeekBar.setSecondaryProgress(((mMusicServiceBinder.getBufferingPercent() * mMusicSeekBar.getMax()) / 100));
    }

    /**
     * refresh Right Text and Seek Bar Max Value
     */
    private void refreshMusicProgressMaxSecond() {
        if (mMusicServiceBinder.isHalfMusicPlayed()) {
            int duration = mMusicServiceBinder.getMediaPlayer().getDuration();
            duration /= 1000;
            mMusicSeekBar.setMax(duration);
            mMusicEndTimeText.setText(GlobalTool.secondToMinSecText(duration));
        } else {
            mMusicSeekBar.setMax(0);
            mMusicEndTimeText.setText("00:00");
        }
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

    private void showInputNewListName() {
        final EditText editText = new EditText(MusicPlayerActivity.this);

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MusicPlayerActivity.this);
        inputDialog.setTitle("Input List Name").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            mMusicListServiceBinder.createMusicList(editText.getText().toString());
            mPlayerBottomListAdaptor.add(mMusicListServiceBinder.getLastMusicList());
            mPlayerBottomListAdaptor.notifyDataSetChanged();
            mMusicListServiceBinder.saveMusicLists();
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }

    public void showShortToast(final String msg) {
        runOnUiThread(() -> {
            Toast.makeText(MusicPlayerActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
