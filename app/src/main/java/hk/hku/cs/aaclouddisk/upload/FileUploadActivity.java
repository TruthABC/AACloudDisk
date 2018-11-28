package hk.hku.cs.aaclouddisk.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;

public class FileUploadActivity extends AppCompatActivity {

    //local cache
    SharedPreferences sharedPreferences;

    //widget
    private ImageView mBackButton;
    private TextView mUploadPathText;
    private EditText mEditTextLocalPath;
//    private Button mButtonSelect;
    private Button mButtonUpload;
    private TextView mUploadingText;

    //Cache on intent extras
    private String relativePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        //Local State load
        sharedPreferences = getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);

        //discard: Aria register (download framework)
//        Aria.download(this).register();
//        Aria.upload(this).register();

        //init widget
        initViews();
        initEvents();
        initFinal();
    }

    private void initViews() {
        mUploadPathText = findViewById(R.id.upload_file_path);
        mBackButton = findViewById(R.id.upload_file_back);
        mEditTextLocalPath = findViewById(R.id.et_local_path);
//        mButtonSelect = findViewById(R.id.btn_select);
        mButtonUpload = findViewById(R.id.btn_upload);
        mUploadingText = findViewById(R.id.uploading_text);
    }

    private void initEvents() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //back to main activity and close itself
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mEditTextLocalPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start a new intent of browsing and selecting a local file
                //  call back: "onActivityResult"
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//set type: any type
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,222);
            }
        });
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileByHttpURLConnection();
            }
        });
    }

    private void initFinal() {
        //get parameters passed by intent
        //  must be passed by main activity and files fragment (right now)
        Bundle extras = getIntent().getExtras();
        relativePath = extras.getString("relativePath","");

        //set vice-title text
        mUploadPathText.setText("Upload To: AACloudDisk\\" + relativePath + "\\");
    }

    /**
     * Use HttpURLConnection to make upload possible
     */
    private void uploadFileByHttpURLConnection() {
        //Prepare params
        String urlParams = "";
        final Map<String, String> params = new HashMap<String, String>();
        String id = sharedPreferences.getString("id","");
        params.put("id", id);
        params.put("relativePath", relativePath);
        try {
            urlParams = "?" + HttpUtilsHttpURLConnection.transformParams(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            showToast("Upload Failed: " + mEditTextLocalPath.getText().toString());
            return;
        }

        //Prepare Url & local paths
        final String uploadUrl = HttpUtilsHttpURLConnection.BASE_URL + "/upload" + urlParams;
        final String localPath = mEditTextLocalPath.getText().toString();

        //Start Upload
        Thread uploadRunnable = new Thread() {
            @Override
            public void run() {
                //show loading info
                showLoading();

                //TODO
//                String response = HttpUtilsHttpURLConnection.uploadByHttp(uploadUrl, new File(localPath));
                String response = "";

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x13;
                Bundle data=new Bundle();
                data.putString("response",response);
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
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //Folder Info result
                            if (response.getErrcode() == 0){
                                showToast("Upload Successful");
                            } else {
                                showToast("Upload Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }

                        //hide loading info
                        hideLoading();
                    }
                }
            };
        };
        uploadRunnable.start();
    }

    private void showLoading() {
        mButtonUpload.setVisibility(View.INVISIBLE);
        mUploadingText.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mButtonUpload.setVisibility(View.VISIBLE);
        mUploadingText.setVisibility(View.INVISIBLE);
    }

    /**
     *  Waring: Discarded
     *  Use 3rd party framework to upload file to server
     */
//    private void uploadFileByAria() {
//        try {
//            Aria.upload(this)
//                    .load(localPath)     //file path local
//                    .setUploadUrl(uploadUrl)  //file upload url
//                    .start();
//        } catch (Exception e) {
//            e.printStackTrace();
//            showToast("Upload Failed: " + mEditTextLocalPath.getText().toString());
//            return;
//        }
//    }

    /**
     *  Call back of other activity's intent back
     *    here we catch the back of selecting files
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // should be our intent's call back, or just return
        if (requestCode != 222) {
            return;
        }
        // intent should be not null
        if (data == null){
            return;
        }
        // should be RESULT_OK
//        if (resultCode == -1) {
//            return;
//        }
        // intent should have data in
        if (data.getData() == null){
            return;
        }

        // try to get file path and revise view
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        uri.getEncodedPath();
        String scheme = uri.getScheme();
        String path = null;
        //if the file is image or something, the process can be complicated as such
        if (scheme == null)
            path = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(
                    uri,
                    new String[] {MediaStore.Images.ImageColumns.DATA }, null, null, null
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        path = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (path == null)
            return;
        mEditTextLocalPath.setText(path);
        mButtonUpload.setVisibility(View.VISIBLE);
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FileUploadActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
