package hk.hku.cs.aaclouddisk.main.tab.files;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.CommonResponse;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;

public class FileInfoListAdapter extends ArrayAdapter<FileInfo> {

    private int mResourceId;
    private MainActivity mActivity;

    public FileInfoListAdapter(Context context, int itemResourceId, MainActivity activity) {
        super(context, itemResourceId);
        this.mResourceId = itemResourceId;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup container){
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(mResourceId, container, false);
        }
        FileInfo fileInfo = getItem(position);

        // set file name
        TextView fileName = convertView.findViewById (R.id.file_name);
        fileName.setText(fileInfo.getName());

        // set file or folder image logo and click event (1/2)
        ImageView deleteLogo = convertView.findViewById(R.id.delete_logo);
        ImageView renameLogo = convertView.findViewById(R.id.rename_logo);
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        deleteLogo.setOnClickListener((v) -> {
            deleteConfirm(fileInfo);
        });
        renameLogo.setOnClickListener((v) -> {
            inputNewName(fileInfo);
        });

        // set file or folder image logo and click event (2/2)
        ImageView fileImage = convertView.findViewById (R.id.file_image);
        // if is a folder
        if (fileInfo.getDir() == 1) {
            //Change to Folder Image
            fileImage.setImageResource(R.drawable.closed22);
        } else { // if not a folder
            //Change to File Image
            fileImage.setImageResource(R.drawable.file77);
        }
        rootItem.setOnClickListener((v) -> {
            // if is a folder
            if (fileInfo.getDir() == 1) {
                Thread waitOneSecondAndGoIntoFolder = new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mActivity.getFileInfoListAndResetAdaptor(fileInfo.getRelativePath());
                });
                waitOneSecondAndGoIntoFolder.start();
            } else {
                //get user id
                SharedPreferences sharedPreferences = mActivity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "");

                //Construct real url
                String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
                String diskRootUrl = baseUrl + "/data/disk/" + id + "/files/";
                String realUrl = diskRootUrl + fileInfo.getRelativePath();
                realUrl = realUrl.replace("\\","/");

                mActivity.downloadInBrowser(realUrl);
            }
        });

        return convertView;
    }

    private void deleteConfirm(final FileInfo fileInfo) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Confirm Delete");
        normalDialog.setMessage("The deletion cannot recover.");
        normalDialog.setPositiveButton("Confirm", (dialog, which) -> {
            doDelete(fileInfo);
        });
        normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        normalDialog.show();
    }

    private void inputNewName(final FileInfo fileInfo) {
        final EditText editText = new EditText(mActivity);

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(mActivity);
        inputDialog.setTitle("Input New Name").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            doRename(fileInfo, editText.getText().toString());
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }

    private void doDelete(final FileInfo fileInfo){
        //get user id
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id", "");

        //Use another thread to do server work
        Thread deleteFileRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/delete_file";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", fileInfo.getRelativePath());

                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x16;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x16){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //change password result
                            if (response.getErrcode() == 0){
                                showToast("Delete File Successful");
                                mActivity.getFileInfoListAndResetAdaptor(mActivity.lastRelativePath);
                            } else {
                                showToast("Delete File Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }
                    }
                }
            };
        };
        deleteFileRunnable.start();
    }

    private void doRename(final FileInfo fileInfo, final String newName) {
        //get user id
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id", "");

        //Use another thread to do server work
        Thread renameRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/rename_file";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", mActivity.lastRelativePath);
                params.put("oldName", fileInfo.getName());
                params.put("newName", newName);


                String response = HttpUtilsHttpURLConnection.postByHttp(url,params);

                //prepare handler bundle data
                Message msg = new Message();
                msg.what=0x17;
                Bundle data=new Bundle();
                data.putString("response",response);
                msg.setData(data);

                //use handler to handle server response
                handler.sendMessage(msg);
            }

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0x17){
                        Bundle data = msg.getData();
                        String responseStr = data.getString("response");//returned json

                        //from String to Object(Entity)
                        try {
                            Gson gson = new Gson();
                            CommonResponse response = gson.fromJson(responseStr, CommonResponse.class);
                            //change password result
                            if (response.getErrcode() == 0){
                                showToast("Rename File Successful");
                                mActivity.getFileInfoListAndResetAdaptor(mActivity.lastRelativePath);
                            } else {
                                showToast("Rename File Failed: " + response.getErrmsg());
                            }
                        } catch (Exception e) {
                            showToast("Network error, plz contact maintenance.");
                        }
                    }
                }
            };
        };
        renameRunnable.start();
    }

    private void showToast(final String msg) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}