package hk.hku.cs.aaclouddisk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;
import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.upload.FileUploadActivity;

import static hk.hku.cs.aaclouddisk.main.TabPagerAdapter.TITLES;

public class MainActivity extends AppCompatActivity {

    //local cache
    SharedPreferences sharedPreferences;

    private Toolbar mToolbar;
    private TextView mTitle;
//    private ImageButton mLeftTopButton;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        //Aria register (download framework)
        Aria.download(this).register();
        Aria.upload(this).register();

        initViews();
        initToolBar();
        initFinal();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) findViewById(R.id.title);
//        mLeftTopButton = (ImageButton) findViewById(R.id.left_top_button);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    private void initToolBar() {
//        mLeftTopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), TaskListActivity.class);
//                startActivityForResult(intent, 0);
//            }
//        });

        // When requested, this adapter returns a specified Fragment(all in package "main.tab"),
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.bold7);
        mTabLayout.getTabAt(1).setIcon(R.drawable.folder99);
        mTabLayout.getTabAt(2).setIcon(R.drawable.user88);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTitle.setText(TITLES[tab.getPosition()]);
                if (tab.getPosition() == 1) {
                    if (lastRelativePath.equals("!@#$%^&*()_+"))
                        getFileInfoListAndResetAdaptor("");
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
        mTitle.setText(TITLES[0]);
    }

    private String lastId = "!@#$%^&*()_+";
    private String lastRelativePath = "!@#$%^&*()_+";
    public void getFileInfoListAndResetAdaptor(final String relativePath) {
        //Use another thread to do server authentication
        Thread getByRelativePathRunnable = new Thread() {
            @Override
            public void run() {
                //get user data
                String id = sharedPreferences.getString("id","");

                //if the same folder for same user, then skip
                if (relativePath.equals(lastRelativePath) && id.equals(lastId)) {
                    return;
                } else {
                    lastRelativePath = relativePath;
                    lastId = id;
                }

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

                                reviseFragmentBar();
                            } else {
                                showToast("Folder Info Get Failed: " + response.getErrmsg());
                            }
                        } catch (JsonSyntaxException e) {
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
    private void reviseFragmentBar() {
        //get Views
        TextView pathTextView = findViewById(R.id.tab_files_title);
        ImageView backImageView = findViewById(R.id.tab_files_back);
        ImageView uploadFileImageView = findViewById(R.id.tab_files_uploadFile);

        //Revise Content
        pathTextView.setText("Path: AACloudDisk\\" + lastRelativePath + "\\");
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
                    //go to upload file page
                    Intent intent = new Intent(v.getContext(), FileUploadActivity.class);
                    intent.putExtra("relativePath", lastRelativePath);
                    startActivityForResult(intent, 0);
                }
            });
        }
    }

    //

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Upload Finish Call back
     * @param task
     */
    @Upload.onTaskComplete public void taskComplete(UploadTask task) {
        String responseStr = task.getEntity().getResponseStr();
        Gson gson = new Gson();
        CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);

        if (response.getErrcode() == 0) {
            showToast("Upload Successful");
        } else {
            showToast("Upload Failed");
        }
    }

}
