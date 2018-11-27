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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.entity.response.FileInfo;
import hk.hku.cs.aaclouddisk.entity.response.FolderInfoResponse;
import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;
import hk.hku.cs.aaclouddisk.tasklist.TaskListActivity;

import static hk.hku.cs.aaclouddisk.main.TabPagerAdapter.TITLES;

public class MainActivity extends AppCompatActivity {

    //local cache
    SharedPreferences sharedPreferences;

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
        mTabLayout.getTabAt(0).setIcon(R.drawable.bold7);
        mTabLayout.getTabAt(1).setIcon(R.drawable.folder99);
        mTabLayout.getTabAt(2).setIcon(R.drawable.user88);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTitle.setText(TITLES[tab.getPosition()]);
                if (tab.getPosition() == 1) {
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

    private void getFileInfoListAndResetAdaptor(final String relativePath) {
        //Use another thread to do server authentication
        Thread getByRelativePathRunnable = new Thread() {
            @Override
            public void run() {
                //get user data
                String id = sharedPreferences.getString("id","");

                String url = HttpUtilsHttpURLConnection.BASE_URL + "/getFolderInfoByRelativePath";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", relativePath);

                String response = HttpUtilsHttpURLConnection.getContextByHttp(url,params);

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
                                    adapter = new FileInfoListAdapter(MainActivity.this, R.layout.tab_files_item);
                                    adapter.addAll(response.getFileInfoList());
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter.clear();
                                    adapter.addAll(response.getFileInfoList());
                                    adapter.notifyDataSetChanged();
                                }
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

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
