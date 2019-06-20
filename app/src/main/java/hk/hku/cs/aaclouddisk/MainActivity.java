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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.FileInfo;
import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3InfoListAdapter;
import hk.hku.cs.aaclouddisk.musicplayer.MusicPlayerActivity;
import hk.hku.cs.aaclouddisk.musicplayer.MusicService;
import hk.hku.cs.aaclouddisk.tasklist.TaskListActivity;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    //Tag
    public static final String TAG = "MainActivity";
    public static final String DEBUG_TAG = "shijian";

    //local cache
    private SharedPreferences sharedPreferences;

    //Views
    private Toolbar mToolbar;
    private TextView mTitle;
    private ImageView mLeftTopButton;
    private RelativeLayout mRightTopButton;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    //Http Response handler
    private MainActivityHandler mMainActivityHandler = new MainActivityHandler(this);
    //Message::what
    private static final int FILE_INFO_LIST_RESP = 0x12;
    private static final int MP3_INFO_LIST_RESP = 0x13;

    //Playing Music
    private MusicService.MusicServiceBinder mMusicServiceBinder;
    //for initializing mMusicServiceBinder;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mMusicServiceBinder = (MusicService.MusicServiceBinder) service;
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate");

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        //Aria register (download framework) discard
//        Aria.download(this).register();
//        Aria.upload(this).register();

        initViews();
        initToolBar();
        initServiceBinder();
        initFinal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        unbindService(this);
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
        mRightTopButton.setOnClickListener((v) -> {
            Intent intent = new Intent(v.getContext(), MusicPlayerActivity.class);
            startActivityForResult(intent, 0);
        });
        // When requested, this adapter returns a specified Fragment(all in package "main.tab"),
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.folder99);
        mTabLayout.getTabAt(1).setIcon(R.drawable.music81);
        mTabLayout.getTabAt(2).setIcon(R.drawable.user88);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTitle.setText(TabPagerAdapter.TITLES[tab.getPosition()]);
                if (tab.getPosition() == 0) {
                    Log.i("shijian", "onFileTabSelected");
                    getFileInfoListAndResetAdaptor(lastRelativePath);
                } else if (tab.getPosition() == 1) {
                    Log.i("shijian", "onMP3TabSelected");
//                    getMP3InfoListAndResetAdaptor();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void initServiceBinder() {
        Intent bindIntent = new Intent(this, MusicService.class);
        bindService(bindIntent, this, BIND_AUTO_CREATE);
    }

    private void initFinal() {
        mTitle.setText(TabPagerAdapter.TITLES[0]);
        mLeftTopButton.setVisibility(View.GONE);//TODO: delete this line in next version :)
    }

    private String lastId = "!@#$%^&*()_+";
    public String lastRelativePath = "";
    public void getFileInfoListAndResetAdaptor(final String relativePath) {
        //Use another thread to do server authentication
        Thread getByRelativePathRunnable = new Thread() {
            @Override
            public void run() {
                //get user data
                String id = sharedPreferences.getString("id","");

                //if the same folder for same user, then skip
//                if (relativePath.equals(lastRelativePath) && id.equals(lastId)) {
//                    return;
//                } else {
                    lastRelativePath = relativePath;
                    lastId = id;
//                }

                String url = HttpUtilsHttpURLConnection.BASE_URL + "/getFolderInfoByRelativePath";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", relativePath);

                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

                //prepare handler bundle data
                Message msg = mMainActivityHandler.obtainMessage();
                msg.what = FILE_INFO_LIST_RESP;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                mMainActivityHandler.sendMessage(msg);
            }

        };
        getByRelativePathRunnable.start();

    }

    /**
     * revise event and text of fragment bar of tab_files
     *    called after response of server of FileInfoList of Folder
     */
    private void reviseFilesFragmentBar() {
        //get Views
        TextView pathTextView = findViewById(R.id.tab_files_title);
        ImageView backImageView = findViewById(R.id.tab_files_back);
        ImageView uploadFileImageView = findViewById(R.id.tab_files_uploadFile);

        //Revise Content
        if (lastRelativePath.length() == 0) {
            pathTextView.setText("Path: AACloudDisk\\");
        } else {
            pathTextView.setText("Path: AACloudDisk\\" + lastRelativePath + "\\");
        }

//        if (lastRelativePath.length()==0) {
//            backImageView.setVisibility(View.INVISIBLE);
//        } else {
//            backImageView.setVisibility(View.VISIBLE);
//        }

        //Event - back
        if (!backImageView.hasOnClickListeners()) {
            backImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastRelativePath.length()==0) {
                        showToast("Cannot Go Back More");
                        return;
                    }

                    //"lastIndex" to check if it is jump with relative path ""
                    String nextPath = "";
                    int lastIndex = lastRelativePath.lastIndexOf("\\");
                    if (lastIndex > 0) {
                        nextPath = lastRelativePath.substring(0, lastIndex);
                    }

                    //back to last page
                    getFileInfoListAndResetAdaptor(nextPath);
                }
            });
        }

        //Event - upload File
        if (!uploadFileImageView.hasOnClickListeners()) {
            uploadFileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to upload file page, could be web view
//                    Intent intent = new Intent(v.getContext(), FileUploadActivity.class);
//                    intent.putExtra("relativePath", lastRelativePath);
//                    startActivityForResult(intent, 0);
                    //go to upload file web page
                    //get user data
                    String id = sharedPreferences.getString("id","");
                    String iii = HttpUtilsHttpURLConnection.BASE_URL + "/upload_file.html?id=" + URLEncoder.encode(id) + "&relativePath=" + URLEncoder.encode(lastRelativePath);
                    Uri uri = Uri.parse(iii);
                    Intent intent  = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Use Browser to download
     * @param url target url
     */
    public void downloadInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent  = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * called by MP3FragmentCreated
     */
    public void getMP3InfoListAndResetAdaptor() {
        //Use another thread to do server authentication
        Thread getAllMP3InfoRunnable = new Thread() {
            @Override
            public void run() {
                //get user data
                String id = sharedPreferences.getString("id","");

                String url = HttpUtilsHttpURLConnection.BASE_URL + "/getAllMP3InfoById";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);

                String response = HttpUtilsHttpURLConnection.postByHttp(url, params);

                //prepare handler bundle data
                Message msg = mMainActivityHandler.obtainMessage();
                msg.what = MP3_INFO_LIST_RESP;
                Bundle data = new Bundle();
                data.putString("response", response);
                msg.setData(data);

                //use handler to handle server response
                mMainActivityHandler.sendMessage(msg);
            }
        };

        getAllMP3InfoRunnable.start();
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
     * The Thread Message Handler (Should be "static", so "inner" class)
     *  Eg. handling HTTP responses
     *  Hint: Instances of static inner classes do not hold an implicit reference to their outer class.
     *  Hint: In Java, non-static inner and anonymous classes hold an implicit reference to their outer class.
     *        Static inner classes, on the other hand, do not.
     */
    private static class MainActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MainActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity == null) {
                // cannot be here
                Log.e("shijian", "MainActivityHandler 'activity == null'");
                return;
            }
            if (msg.what == FILE_INFO_LIST_RESP) {
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    FolderInfoResponse response = gson.fromJson(responseStr, FolderInfoResponse.class);
                    //Folder Info result
                    if (response.getErrcode() == 0){
//                                showToast("Folder Info Get Successful");
                        //find View and then its Adapter
                        ListView listView = (ListView) activity.findViewById(R.id.list_view_files);
                        FileInfoListAdapter adapter = (FileInfoListAdapter) listView.getAdapter();

                        //if no File
                        if (response.getFileInfoList().size() == 0) {
                            activity.findViewById(R.id.no_file_hint).setVisibility(View.VISIBLE);
                        } else {
                            activity.findViewById(R.id.no_file_hint).setVisibility(View.GONE);
                        }

                        //apply changes and call adapter to change
                        if (adapter == null) {
                            adapter = new FileInfoListAdapter(activity, R.layout.tab_files_item, activity);
                            adapter.addAll(response.getFileInfoList());
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter.clear();
                            adapter.addAll(response.getFileInfoList());
                            adapter.notifyDataSetChanged();
                        }

                        activity.reviseFilesFragmentBar();
                    } else {
                        activity.showToast("Folder Info Get Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }

                //hide loading box
//                        hideLoading();
            } else if (msg.what == MP3_INFO_LIST_RESP) {
                Bundle data = msg.getData();
                String responseStr = data.getString("response");//returned json

                //from String to Object(Entity)
                try {
                    Gson gson = new Gson();
                    FolderInfoResponse response = gson.fromJson(responseStr, FolderInfoResponse.class);
                    //Info result
                    if (response.getErrcode() == 0){
                        //find View and then its Adapter
                        ListView listView = (ListView) activity.findViewById(R.id.list_view_mp3);
                        MP3InfoListAdapter adapter = (MP3InfoListAdapter) listView.getAdapter();

                        //if no File
                        if (response.getFileInfoList().size() == 0) {
                            activity.findViewById(R.id.no_mp3_hint).setVisibility(View.VISIBLE);
                        } else {
                            activity.findViewById(R.id.no_mp3_hint).setVisibility(View.GONE);
                        }

                        //apply changes and call adapter to change
                        if (adapter == null) {
                            adapter = new MP3InfoListAdapter(activity, R.layout.tab_mp3_item, activity);
                            adapter.addAll(response.getFileInfoList());
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter.clear();
                            adapter.addAll(response.getFileInfoList());
                            adapter.notifyDataSetChanged();
                        }

                        //TODO: refactor me, e.g. research on whether UI thread pending
                        //Try to set MusicService Resource List
                        if (activity.mMusicServiceBinder != null) {
                            Log.i(DEBUG_TAG, "MusicService Ready.");
                            if (activity.mMusicServiceBinder.getResourceList() == null || activity.mMusicServiceBinder.getResourceList().size() == 0) {
                                Log.i(DEBUG_TAG, "Set resource List.");
                                List<String> resourceList = new ArrayList<>();
                                for (FileInfo fileInfo: response.getFileInfoList()) {
                                    SharedPreferences sharedPreferences = activity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
                                    String id = sharedPreferences.getString("id", "");

                                    String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
                                    String diskRootUrl = baseUrl + "/data/disk/" + id + "/files/";
                                    String realUrl = diskRootUrl + fileInfo.getRelativePath();

                                    resourceList.add(realUrl);
                                }
                                activity.mMusicServiceBinder.setResourceList(resourceList);
                            }
                        } else {
                            Log.i(DEBUG_TAG, "MusicService not Ready, but needed by set resource List.");
                        }
                    } else {
                        activity.showToast("MP3 Info Get Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.showToast("Network error, plz contact maintenance.");
                }
            }// if (msg.what == MP3_INFO_LIST_RESP)
        }// MainActivityHandler.handleMessage(Message msg)
    }// MainActivityHandler

}
