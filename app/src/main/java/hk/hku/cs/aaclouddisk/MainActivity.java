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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.main.tab.mp3.MP3InfoListAdapter;
import hk.hku.cs.aaclouddisk.tasklist.TaskListActivity;

import static hk.hku.cs.aaclouddisk.main.TabPagerAdapter.TITLES;

public class MainActivity extends AppCompatActivity {

    //local cache
    private SharedPreferences sharedPreferences;

    private Toolbar mToolbar;
    private TextView mTitle;
    private ImageButton mLeftTopButton;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

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
                mTitle.setText(TITLES[tab.getPosition()]);
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
        mTitle.setText(TITLES[0]);
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
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x12){
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
                                ListView listView = (ListView) findViewById(R.id.list_view_files);
                                FileInfoListAdapter adapter = (FileInfoListAdapter) listView.getAdapter();

                                //if no File
                                if (response.getFileInfoList().size() == 0) {
                                    findViewById(R.id.no_file_hint).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.no_file_hint).setVisibility(View.GONE);
                                }

                                //apply changes and call adapter to change
                                if (adapter == null) {
                                    adapter = new FileInfoListAdapter(MainActivity.this, R.layout.tab_files_item, MainActivity.this);
                                    adapter.addAll(response.getFileInfoList());
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter.clear();
                                    adapter.addAll(response.getFileInfoList());
                                    adapter.notifyDataSetChanged();
                                }

                                reviseFilesFragmentBar();
                            } else {
                                showToast("Folder Info Get Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }

                        //hide loading box
//                        hideLoading();
                    }
                }
            };
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
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x13){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            FolderInfoResponse response = gson.fromJson(responseStr, FolderInfoResponse.class);
                            //Info result
                            if (response.getErrcode() == 0){
                                //find View and then its Adapter
                                ListView listView = (ListView) findViewById(R.id.list_view_mp3);
                                MP3InfoListAdapter adapter = (MP3InfoListAdapter) listView.getAdapter();

                                //if no File
                                if (response.getFileInfoList().size() == 0) {
                                    findViewById(R.id.no_mp3_hint).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.no_mp3_hint).setVisibility(View.GONE);
                                }

                                //apply changes and call adapter to change
                                if (adapter == null) {
                                    adapter = new MP3InfoListAdapter(MainActivity.this, R.layout.tab_mp3_item, MainActivity.this);
                                    adapter.addAll(response.getFileInfoList());
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter.clear();
                                    adapter.addAll(response.getFileInfoList());
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                showToast("MP3 Info Get Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }
                    }
                }
            };
        };
        getAllMP3InfoRunnable.start();
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Warning: Discard: now use browser to do it
     * Use DownloadManager to download
     * @param url target url
     * @param name filename
     */
//    public void download(String url, String name) {
//        showToast("Download Started");
//        try {
//            //创建下载任务,downloadUrl就是下载链接
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//            //下载中和下载完后都显示通知栏
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            //指定下载路径和下载文件名
//            request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, name);
//            //通知栏标题
//            request.setTitle(name);
//            //通知栏描述信息
//            request.setDescription("DownLoad Of AACloudDisk");
//            //获取下载管理器
//            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//            //将下载任务加入下载队列，否则不会进行下载
//            long downloadTaskId = downloadManager.enqueue(request);
//        } catch (Exception e) {
//            e.printStackTrace();
//            showToast("Start Downloading Failed");
//        }
//
//    }

}
