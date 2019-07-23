package hk.hku.cs.aaclouddisk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.musicplayer.MusicList;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;
import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;
import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.FilesFragment;
import hk.hku.cs.aaclouddisk.main.tab.MP3Fragment;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3InfoListAdapter;
import hk.hku.cs.aaclouddisk.musicplayer.MusicListService;
import hk.hku.cs.aaclouddisk.musicplayer.MusicPlayerActivity;
import hk.hku.cs.aaclouddisk.musicplayer.MusicService;
import hk.hku.cs.aaclouddisk.tasklist.TaskListActivity;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    //Tag
    public static final String TAG = "MainActivity";
    public static final String DEBUG_TAG = "shijian";

    //local cache
    private SharedPreferences sharedPreferences;
    public String userId;
    public String lastRelativePath;

    //Views
    private Toolbar mToolbar;
    private TextView mTitle;
    private ImageView mLeftTopButton;
    private RelativeLayout mRightTopButton;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    public TabPagerAdapter mTabPagerAdapter;

    //Playing Music & Music List
    public MusicService.MusicServiceBinder mMusicServiceBinder = null;
    public MusicListService.MusicListServiceBinder mMusicListServiceBinder = null;

    //for "Add music to music list"
    public int clickedMusicIndex = -1;

    //Http Response handler
    private MainActivityHandler mMainActivityHandler = new MainActivityHandler(MainActivity.this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate");

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("id", "");
        lastRelativePath = "";

        initViews();
        initToolBar();
        initAllServiceBinder();
        initFinal();
    }

    //for initializing mMusicServiceBinder;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof MusicService.MusicServiceBinder) {
            mMusicServiceBinder = (MusicService.MusicServiceBinder) service;
        } else if (service instanceof MusicListService.MusicListServiceBinder) {
            mMusicListServiceBinder = (MusicListService.MusicListServiceBinder) service;
        }
        //Do another init after two service both ready
        if (mMusicServiceBinder != null && mMusicListServiceBinder != null) {
            MainActivity.this.getMP3InfoListAndHandle();
        }
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        unbindService(MainActivity.this);
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) findViewById(R.id.title);
        mLeftTopButton = (ImageView) findViewById(R.id.left_top_button);
        mRightTopButton = (RelativeLayout) findViewById(R.id.right_top_button_wrapper);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    private void initToolBar() {
        mLeftTopButton.setOnClickListener((v) -> {
            Intent intent = new Intent(v.getContext(), TaskListActivity.class);
            startActivityForResult(intent, 0);
        });
        mRightTopButton.setVisibility(View.INVISIBLE);
        mRightTopButton.setOnClickListener((v) -> {
            Intent intent = new Intent(v.getContext(), MusicPlayerActivity.class);
            startActivityForResult(intent, 0);
        });
        // When requested, this adapter returns a specified Fragment(all in package "main.tab"),
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.music81);
        mTabLayout.getTabAt(1).setIcon(R.drawable.folder99);
        mTabLayout.getTabAt(2).setIcon(R.drawable.user88);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTitle.setText("AA Cloud " + TabPagerAdapter.TITLES[tab.getPosition()]);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

    }

    private void initAllServiceBinder() {
        mMusicServiceBinder = null;
        mMusicListServiceBinder = null;
        //bind MusicService
        Intent bindIntent = new Intent(MainActivity.this, MusicService.class);
        bindService(bindIntent, MainActivity.this, BIND_AUTO_CREATE);
        //then bind MusicListService
        Intent bindIntent2 = new Intent(MainActivity.this, MusicListService.class);
        bindService(bindIntent2, MainActivity.this, BIND_AUTO_CREATE);
    }

    private void initFinal() {
        mTitle.setText("AA Cloud " + TabPagerAdapter.TITLES[0]);
        mLeftTopButton.setVisibility(View.GONE);//TODO: delete this line in download/upload management version :)
    }
    
    /**
     * Use Browser to Download or Play/Preview
     * @param url target url
     */
    public void downloadInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent  = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Called when get music files failed or Network failed (@Notnull Services)
     */
    private void applyOfflineMusicList() {
        //Context
        MP3Fragment mp3Fragment = mTabPagerAdapter.getMP3Fragment();
        MP3InfoListAdapter adapter = mp3Fragment.mBodyListAdaptor;
        adapter.clear();
        mp3Fragment.mNoMP3Hint.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();

        List<MusicList> musicLists = mMusicListServiceBinder.getMusicLists();
        if (musicLists.size() == 0) {
            mMusicListServiceBinder.initOnlineList(new ArrayList<>());
            mMusicListServiceBinder.saveMusicLists();
        }
        mMusicServiceBinder.setResourceListByMusicList(musicLists.get(0), 0);
        mRightTopButton.setVisibility(View.VISIBLE);
    }

    /**
     * Called when get music files info (@Notnull Services)
     */
    private void applyOnlineMusicList(List<FileInfo> fileInfoList) {
        //Context
        MP3Fragment mp3Fragment = mTabPagerAdapter.getMP3Fragment();
        MP3InfoListAdapter adapter = mp3Fragment.mBodyListAdaptor;

        //if no Music File
        if (fileInfoList.size() == 0) {
            adapter.clear();
            mp3Fragment.mNoMP3Hint.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            adapter.clear();
            adapter.addAll(fileInfoList);
            mp3Fragment.mNoMP3Hint.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }

        // if Services All Ready (Bug-fix-190723: It Must Be Ready in Logic)
        if (mMusicServiceBinder != null && mMusicListServiceBinder != null) {
            //Construct Resource List
            List<ResourceInfo> resourceList = new ArrayList<>();
            for (FileInfo fileInfo: fileInfoList) {
                String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
                String diskRootUrl = baseUrl + "/data/disk/" + userId + "/files/";
                String realUrl = diskRootUrl + fileInfo.getRelativePath();
                realUrl = realUrl.replace("\\","/");

                resourceList.add(new ResourceInfo(fileInfo.getName(), realUrl, false, null));
            }

            //For MusicListService
            List<MusicList> musicLists = mMusicListServiceBinder.getMusicLists();
            if (musicLists.size() == 0) {
                mMusicListServiceBinder.initOnlineList(resourceList);
                mMusicListServiceBinder.saveMusicLists();
            } else {
                mMusicListServiceBinder.updateOnlineList(resourceList);
                mMusicListServiceBinder.saveMusicLists();
            }
            //For MusicService
            mMusicServiceBinder.setResourceListByMusicList(musicLists.get(0), 0);
            mRightTopButton.setVisibility(View.VISIBLE);
            Log.i(TAG, "Service Ready. Apply online resource List.");
        } else { //should not be here
            Log.i(TAG, "Service not Ready, should not be here handling network response.");
        }
    }

    /**
     * Finish Activity and Go Back to Login
     */
    public void logoutAndForward() {
        MainActivity.this.finish();
    }
    
    public void showShortToast(final String msg) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }
    
    public void showToast(final String msg) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * called by FilesFragment
     * @param relativePath the relative path in user's own cloud disk
     */
    public void getFileInfoListAndResetAdaptor(String relativePath) {
        //Use another thread to do server authentication
        Thread getByRelativePathRunnable = new Thread(() -> {
            lastRelativePath = relativePath;

            String url = HttpUtilsHttpURLConnection.BASE_URL + "/getFolderInfoByRelativePath";
            Map<String, String> params = new HashMap<>();
            params.put("id", userId);
            params.put("relativePath", relativePath);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mMainActivityHandler.obtainMessage();
            msg.what = MainActivityHandler.FILE_INFO_LIST_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mMainActivityHandler.sendMessage(msg);
        });
        getByRelativePathRunnable.start();
    }

    /**
     * called by MP3Fragment InitFinal & Service All Ready
     */
    public void getMP3InfoListAndHandle() {
        //If Service or Fragment not ready, Halt
        if (mMusicServiceBinder == null || mMusicListServiceBinder == null || mTabPagerAdapter.getMP3Fragment() == null) {
            return;
        }
        //Use another thread to do server authentication
        Thread getAllMP3InfoRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/getAllMP3InfoById";
            Map<String, String> params = new HashMap<>();
            params.put("id", userId);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mMainActivityHandler.obtainMessage();
            msg.what = MainActivityHandler.MP3_INFO_LIST_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mMainActivityHandler.sendMessage(msg);
        });
        getAllMP3InfoRunnable.start();
    }

    /**
     * called by FilesFragment
     */
    public void createFolderAndHandle() {
        //Use another thread to do server work
        Thread newFolderRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/create_folder";
            Map<String, String> params = new HashMap<>();
            params.put("id", userId);
            params.put("relativePath", lastRelativePath);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mMainActivityHandler.obtainMessage();
            msg.what = MainActivityHandler.CREATE_FOLDER_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mMainActivityHandler.sendMessage(msg);
        });
        newFolderRunnable.start();
    }

    /**
     * called by FilesFragment -> ListView -> Adaptor
     */
    public void deleteFileAndHandle(FileInfo fileInfo) {
        //Use another thread to do server work
        Thread deleteFileRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/delete_file";
            Map<String, String> params = new HashMap<>();
            params.put("id", userId);
            params.put("relativePath", fileInfo.getRelativePath());

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mMainActivityHandler.obtainMessage();
            msg.what = MainActivityHandler.DELETE_FILE_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mMainActivityHandler.sendMessage(msg);
        });
        deleteFileRunnable.start();
    }

    /**
     * called by FilesFragment -> ListView -> Adaptor
     */
    public void renameFileAndHandle(FileInfo fileInfo, String newName) {
        if (fileInfo.getName().equals(newName)) {
            return;
        }
        //Use another thread to do server work
        Thread renameRunnable = new Thread(() -> {
            String url = HttpUtilsHttpURLConnection.BASE_URL + "/rename_file";
            Map<String, String> params = new HashMap<>();
            params.put("id", userId);
            params.put("relativePath", lastRelativePath);
            params.put("oldName", fileInfo.getName());
            params.put("newName", newName);

            String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

            //prepare handler bundle data
            Message msg = mMainActivityHandler.obtainMessage();
            msg.what = MainActivityHandler.RENAME_FILE_RESP;
            Bundle data = new Bundle();
            data.putString("response", response);
            msg.setData(data);

            //use handler to handle server response
            mMainActivityHandler.sendMessage(msg);
        });
        renameRunnable.start();
    }

    /**
     * called by MeFragment
     */
    public void changePasswordAndHandle(final String newPassword) {
        //should not be empty
        if (newPassword.equals("")) {
            showToast("Password cannot be empty");
            return;
        }

        //Use another thread to do server work
        Thread changePasswordRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/update_password";
                Map<String, String> params = new HashMap<>();
                params.put("id", userId);
                params.put("password", newPassword);

                String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

                //prepare handler bundle data
                Message msg = mMainActivityHandler.obtainMessage();
                msg.what = MainActivityHandler.CHANGE_PASSWORD_RESP;
                Bundle data = new Bundle();
                data.putString("response", response);
                msg.setData(data);

                //use handler to handle server response
                mMainActivityHandler.sendMessage(msg);
            }
        };
        changePasswordRunnable.start();
    }
    
    /**
     * The Thread Message Handler (Should be "static", so "inner" class)
     *  Eg. handling HTTP responses
     *  Hint: Instances of static inner classes do not hold an implicit reference to their outer class.
     *  Hint: In Java, non-static inner and anonymous classes hold an implicit reference to their outer class.
     *        Static inner classes, on the other hand, do not.
     */
    private static class MainActivityHandler extends Handler {
        //Message::what
        private static final int FILE_INFO_LIST_RESP = 0x12;
        private static final int MP3_INFO_LIST_RESP = 0x13;
        private static final int DELETE_FILE_RESP = 0x16;
        private static final int RENAME_FILE_RESP = 0x17;
        private static final int CREATE_FOLDER_RESP = 0x18;
        private static final int CHANGE_PASSWORD_RESP = 0x22;

        private final WeakReference<MainActivity> mActivityRef;

        public MainActivityHandler(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //Context
            MainActivity activity = mActivityRef.get();
            if (activity == null) {
                // cannot be here
                Log.e(DEBUG_TAG, "MainActivityHandler 'activity == null'");
                return;
            }

            //Which Message switch:
            if (msg.what == FILE_INFO_LIST_RESP) {
                //Init response data
                Bundle data = msg.getData();
                String responseStr = data.getString("response");

                //Thread for slow Gson convert
                Thread handleResponseThread = new Thread(() -> {
                    Log.i(TAG, "FILE_INFO_LIST_RESP ThreadName: " + Thread.currentThread().getName());

                    //convert from String to Object(Entity)
                    try {
                        Gson gson = new Gson();
                        FolderInfoResponse response = gson.fromJson(responseStr, FolderInfoResponse.class);

                        //After Gson convert do UI things
                        activity.runOnUiThread(() -> {
                            Log.i(TAG, "After Gson Converted ThreadName: " + Thread.currentThread().getName());
                            //Context
                            FilesFragment filesFragment = activity.mTabPagerAdapter.getFilesFragment();
                            FileInfoListAdapter adapter = filesFragment.mFileListAdaptor;

                            //Folder Info result
                            if (response.getErrcode() == 0){
                                Log.i(TAG,"Folder Info Get Successful, getErrcode() == 0");

                                //Revise Subtitle Content
                                String newPathText = "[Path]AACloudDisk\\" + activity.lastRelativePath;
                                filesFragment.mPathTextView.setText(newPathText);

                                //Revise File List
                                adapter.clear();
                                adapter.addAll(response.getFileInfoList());
                                adapter.notifyDataSetChanged();
                            } else {
                                activity.showToast("Folder Info Get Failed: " + response.getErrmsg());
                            }

                            //Revise No File Hint
                            if (adapter.isEmpty()) {
                                filesFragment.mNoFileHint.setVisibility(View.VISIBLE);
                            } else {
                                filesFragment.mNoFileHint.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        activity.showToast("Network error, plz contact maintenance.");
                    }
                });
                handleResponseThread.start();
                /* if (msg.what == FILE_INFO_LIST_RESP) */
            } else if (msg.what == MP3_INFO_LIST_RESP) {
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json
                Thread handleResponseThread = new Thread(() -> {
                    Log.i(TAG, "MP3_INFO_LIST_RESP ThreadName: " + Thread.currentThread().getName());
                    //from String to Object(Entity)
                    try {
                        Gson gson = new Gson();
                        FolderInfoResponse response = gson.fromJson(responseStr, FolderInfoResponse.class);
                        //Info result
                        if (response.getErrcode() == 0){
                            activity.runOnUiThread(() -> { activity.applyOnlineMusicList(response.getFileInfoList()); });
                        } else {
                            activity.showToast("MP3 Info Get Failed: " + response.getErrmsg());
                            activity.runOnUiThread(activity::applyOfflineMusicList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        activity.showToast("Network error, plz contact maintenance.");
                        activity.runOnUiThread(activity::applyOfflineMusicList);
                    }
                });
                handleResponseThread.start();
                /* if (msg.what == MP3_INFO_LIST_RESP)*/
            } else if (msg.what == CREATE_FOLDER_RESP) {
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //result
                    if (response.getErrcode() == 0){
                        activity.showToast("Create Folder Successful");
                        activity.getFileInfoListAndResetAdaptor(activity.lastRelativePath);
                    } else {
                        activity.showToast("Create Folder Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }
                /* if (msg.what == CREATE_FOLDER_RESP)*/
            } else if (msg.what == DELETE_FILE_RESP){
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //change password result
                    if (response.getErrcode() == 0){
                        activity.showToast("Delete File Successful");
                        activity.getFileInfoListAndResetAdaptor(activity.lastRelativePath);
                    } else {
                        activity.showToast("Delete File Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }
                /* if (msg.what == DELETE_FILE_RESP)*/
            } else if (msg.what == RENAME_FILE_RESP){
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //change password result
                    if (response.getErrcode() == 0){
                        activity.showToast("Rename File Successful");
                        activity.getFileInfoListAndResetAdaptor(activity.lastRelativePath);
                    } else {
                        activity.showToast("Rename File Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }
                /* if (msg.what == RENAME_FILE_RESP)*/
            } else if (msg.what == CHANGE_PASSWORD_RESP){
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                    //change password result
                    if (response.getErrcode() == 0){
                        activity.showToast("Change Password Successful");
                        activity.logoutAndForward();
                    } else {
                        activity.showToast("Change Password Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }
                /* if (msg.what == CHANGE_PASSWORD_RESP)*/
            }
        }// MainActivityHandler.handleMessage(Message msg)
    }// MainActivityHandler
}