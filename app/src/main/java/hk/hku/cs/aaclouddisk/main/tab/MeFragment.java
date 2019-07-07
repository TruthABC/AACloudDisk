package hk.hku.cs.aaclouddisk.main.tab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.GlobalTool;
import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;
import hk.hku.cs.aaclouddisk.musicplayer.MusicService;

public class MeFragment extends Fragment {

    //Tag
    public static final String TAG = "MeFragment";
    public static final String DEBUG_TAG = "shijian";

    //Views
    private Button mBtnLogout;
    private Button mBtnChangePw;
    private Button mBtnClrCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_me, container, false);

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
            logoutAndForward();
        });
        mBtnChangePw.setOnClickListener((v) -> {
            inputPassword();
        });
        mBtnClrCache.setOnClickListener((v) -> {
            clearConfirm();
        });
    }

    /**
     * go to login view
     */
    private void logoutAndForward() {
        getActivity().finish();
    }

    /**
     *  input new password dialog
     */
    private void inputPassword() {
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setTypeface(Typeface.DEFAULT);

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(getActivity());
        inputDialog.setTitle("Input New Password").setView(editText);
        inputDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changePassword(editText.getText().toString());
                    }
                });
        inputDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
        inputDialog.show();
    }

    /**
     * handle change password
     * @param newPassword
     */
    private void changePassword(final String newPassword) {

        //get user id
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id","");

        //should not be empty
        if (newPassword.equals("")) {
            showToast("Password cannot be empty");
            return;
        }

        //Set button non-clickable
        mBtnChangePw.setClickable(false);
        mBtnLogout.setClickable(false);

        //Use another thread to do server work
        Thread changePasswordRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/update_password";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("password", newPassword);

                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x22;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x22){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //change password result
                            if (response.getErrcode() == 0){
                                showToast("Change Password Successful");
                                logoutAndForward();
                            } else {
                                showToast("Change Password Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }

                        //Set button back to clickable
                        mBtnChangePw.setClickable(true);
                        mBtnLogout.setClickable(true);
                    }
                }
            };
        };
        changePasswordRunnable.start();
    }

    /**
     * Confirmation of clear music cache
     */
    private void clearConfirm() {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getActivity());
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Clear Music Cache");
        normalDialog.setMessage("The music cache clearance cannot be recovered.");
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
        MainActivity activity = (MainActivity) getActivity();
        MusicService.MusicServiceBinder musicServiceBinder = activity.mMusicServiceBinder;
        if (musicServiceBinder == null) {
            showToast("Cache Service not ready, please wait a moment.");
            return;
        }
        HttpProxyCacheServer proxy = musicServiceBinder.getProxy();
        File cacheRoot = GlobalTool.getIndividualCacheDirectory(activity);
        Log.i(TAG, "Cache Root: " + cacheRoot.getAbsolutePath());
        GlobalTool.deleteDir(cacheRoot);
        showToast("Music Cache Cleared.");
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        });
    }
}
