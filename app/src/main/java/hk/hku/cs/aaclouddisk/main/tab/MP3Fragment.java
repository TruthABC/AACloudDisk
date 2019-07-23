package hk.hku.cs.aaclouddisk.main.tab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3BottomListAdaptor;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3InfoListAdapter;

public class MP3Fragment extends Fragment {

    //Tag
    public static final String TAG = "MP3Fragment";
    public static final String DEBUG_TAG = "shijian";

    //Context: Parent Activity
    public MainActivity mActivity;

    //Views
    public TextView mNoMP3Hint;
    private ListView mBodyListView;
    public MP3InfoListAdapter mBodyListAdaptor;

    //Views - Bottom Sheet
    private boolean shownBottom = false;
    private RelativeLayout mBottomSheet;
    private BottomSheetBehavior<RelativeLayout> mBottomSheetBehavior;
    private RelativeLayout mHideBottomSheetButtonWrapper;
    private RelativeLayout mCreateMusiListButtonWrapper;

    //Views - Bottom Sheet Lists
    private ListView mBottomListView;
    private MP3BottomListAdaptor mBottomListAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_mp3, container, false);

        mActivity = (MainActivity) getActivity();

        initViews(rootView);
        initBottomSheet();
        initEvents();
        initFinal();

        return rootView;
    }

    private void initViews(View v) {
        //Body Views
        mNoMP3Hint = (TextView) v.findViewById(R.id.no_mp3_hint);
        mBodyListAdaptor = new MP3InfoListAdapter(getContext(), R.layout.tab_mp3_item, mActivity);
        mBodyListView = (ListView) v.findViewById(R.id.list_view_mp3);
        mBodyListView.setAdapter(mBodyListAdaptor);

        //Bottom Sheet
        mBottomSheet = (RelativeLayout) v.findViewById(R.id.music_tab_bottom_sheet);

        //Bottom Sheet Header (topBar, not "Peek Height" part)
        mHideBottomSheetButtonWrapper = (RelativeLayout) v.findViewById(R.id.music_tab_bottom_right_top_button_wrapper);
        mCreateMusiListButtonWrapper = (RelativeLayout) v.findViewById(R.id.music_tab_bottom_left_top_button_wrapper);

        //Music List lists
        mBottomListAdaptor = new MP3BottomListAdaptor(getContext(), R.layout.tab_mp3_bottom_item, mActivity);
        mBottomListView = (ListView) v.findViewById(R.id.music_tab_bottom_list_view);
        mBottomListView.setAdapter(mBottomListAdaptor);
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
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        mBottomSheetBehavior.setFitToContents(false);
        mBottomSheetBehavior.setHideable(true);//prevents the bottom sheet from completely hiding off the screen
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);//initially state
        shownBottom = false;
    }

    private void initEvents() {
        //Bottom Sheet Events
        mHideBottomSheetButtonWrapper.setOnClickListener((v) -> {
            hideBottom();
        });
        mCreateMusiListButtonWrapper.setOnClickListener((v) -> {
            showInputNewListName();
        });
    }

    /**
     * In case Tab Selected off and on afterwards
     */
    private void initFinal() {
        Log.i(TAG, "initFinal");
        // Initialize music list at very beginning, even user not switched to this tab.
        if (mActivity.mMusicListServiceBinder != null) {
            Log.i(TAG, "mBottomListAdaptor init");
            mBottomListAdaptor.clear();
            mBottomListAdaptor.addAll(mActivity.mMusicListServiceBinder.getMusicLists());
            mBottomListAdaptor.notifyDataSetChanged();
        }
        mActivity.getMP3InfoListAndHandle();
        mActivity.clickedMusicIndex = -1;
    }

    private void showInputNewListName() {
        final EditText editText = new EditText(mActivity);

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(mActivity);
        inputDialog.setTitle("New Music List Name:").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            mActivity.mMusicListServiceBinder.createMusicList(editText.getText().toString());
            mBottomListAdaptor.add(mActivity.mMusicListServiceBinder.getLastMusicList());
            mBottomListAdaptor.notifyDataSetChanged();
            mActivity.mMusicListServiceBinder.saveMusicLists();
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }

    public void showBottom() {
        // Initialize music list
        if (mActivity.mMusicListServiceBinder != null) {
            Log.i(TAG, "mBottomListAdaptor init");
            mBottomListAdaptor.clear();
            mBottomListAdaptor.addAll(mActivity.mMusicListServiceBinder.getMusicLists());
            mBottomListAdaptor.notifyDataSetChanged();
        }
        shownBottom = true;
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void hideBottom() {
        shownBottom = false;
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

}