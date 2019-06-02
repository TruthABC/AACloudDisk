package hk.hku.cs.aaclouddisk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3InfoListAdapter;
import hk.hku.cs.aaclouddisk.tasklist.TaskListActivity;

public class MainActivity extends AppCompatActivity {

    //local cache
    private SharedPreferences sharedPreferences;

    private Toolbar mToolbar;
    private TextView mTitle;
    private ImageButton mLeftTopButton;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private MainActivityHandler mMainActivityHandler = new MainActivityHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        //Aria register (download framework) discard
//        Aria.download(this).register();
//        Aria.upload(this).register();

        initViews();
        initToolBar();
        initFinal();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) findViewById(R.id.title);
        mLeftTopButton = (ImageButton) findViewById(R.id.left_top_button);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    private void initToolBar() {
        mLeftTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TaskListActivity.class);
                startActivityForResult(intent, 0);
            }
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
                    getMP3InfoListAndResetAdaptor();
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
                Message msg = new Message();
                msg.what=0x12;
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
     * Play and download music file at the same time (TODO: Research Phase, Hard refactor needed)
     * @param url target url
     */
    public void openMusicFile(String url) {
        showToast("[OJBK]" + url);
    }

    /**
     * called by MP3FragmentCreated or Tab Switched
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
                Message msg = new Message();
                msg.what = 0x13;
                Bundle data = new Bundle();
                data.putString("response", response);
                msg.setData(data);

                //use handler to handle server response
                mMainActivityHandler.sendMessage(msg);
            }
        };

        getAllMP3InfoRunnable.start();
    }

    public void showToast(final String msg) {
        runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        );
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
            if (msg.what == 0x12) {
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
            } else if (msg.what == 0x13) {
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
                    } else {
                        activity.showToast("MP3 Info Get Failed: " + response.getErrmsg());
                    }
                } catch (Exception e) {
                    activity.showToast("Network error, plz contact maintenance.");
                }
            }

        }
    }// private static class MainActivityHandler extends Handler {}

}
