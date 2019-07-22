package hk.hku.cs.aaclouddisk.main.tab;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;

import hk.hku.cs.aaclouddisk.GlobalTool;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.musicplayer.MusicService;

public class MeFragment extends Fragment {

    //Tag
    public static final String TAG = "MeFragment";
    public static final String DEBUG_TAG = "shijian";

    //Context: Parent Activity
    public MainActivity mActivity;

    //Views
    private Button mBtnLogout;
    private Button mBtnChangePw;
    private Button mBtnClrCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_me, container, false);

        mActivity = (MainActivity) getActivity();

        initViews(rootView);
        initEvents();

        return rootView;
    }

    private void initViews(View v) {
        mBtnLogout = v.findViewById(R.id.btn_logout);
        mBtnChangePw = v.findViewById(R.id.btn_change_password);
        mBtnClrCache = v.findViewById(R.id.btn_clear_cache);
    }

    private void initEvents() {
        mBtnLogout.setOnClickListener((v) -> {
            mActivity.logoutAndForward();
        });
        mBtnChangePw.setOnClickListener((v) -> {
            inputPassword();
        });
        mBtnClrCache.setOnClickListener((v) -> {
            clearConfirm();
        });
    }

    /**
     *  input new password dialog
     */
    private void inputPassword() {
        final EditText editText = new EditText(mActivity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setTypeface(Typeface.DEFAULT);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mActivity);
        inputDialog.setTitle("Input New Password").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            mActivity.changePasswordAndHandle(editText.getText().toString());
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }

    /**
     * Confirmation of clear music cache
     */
    private void clearConfirm() {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Clear Music Cache");
        normalDialog.setMessage("The cache cannot be recovered.");
        normalDialog.setPositiveButton("Confirm", (dialog, which) -> {
            clearMusicCache();
        });
        normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        normalDialog.show();
    }

    /**
     * clear music cache
     */
    private void clearMusicCache() {
        MusicService.MusicServiceBinder musicServiceBinder = mActivity.mMusicServiceBinder;
        if (musicServiceBinder == null) {
            mActivity.showToast("Cache Service not ready, please wait a moment.");
            return;
        }
        HttpProxyCacheServer proxy = musicServiceBinder.getProxy();
        File cacheRoot = GlobalTool.getIndividualCacheDirectory(mActivity);
        Log.i(TAG, "Cache Root: " + cacheRoot.getAbsolutePath());
        GlobalTool.deleteDir(cacheRoot);
        mActivity.showToast("Music Cache Cleared.");
    }
}
