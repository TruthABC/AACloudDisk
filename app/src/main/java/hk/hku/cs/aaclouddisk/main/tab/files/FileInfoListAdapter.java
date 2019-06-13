package hk.hku.cs.aaclouddisk.main.tab.files;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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

    private int resourceId;
    private AppCompatActivity activity;

    public FileInfoListAdapter(Context context, int resourceId, AppCompatActivity activity) {
        super(context, resourceId);
        this.resourceId = resourceId;
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final FileInfo fileInfo = getItem(position);
        View v = LayoutInflater.from (getContext()).inflate (resourceId, parent, false);

        // set file name
        TextView fileName = v.findViewById (R.id.file_name);
        fileName.setText(fileInfo.getName());

        // set file or folder image logo and click event (1/2)
        ImageView deleteLogo = v.findViewById(R.id.delete_logo);
        ImageView renameLogo = v.findViewById(R.id.rename_logo);
        deleteLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirm(fileInfo);
            }
        });
        renameLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputNewName(fileInfo);
            }
        });

        // set file or folder image logo and click event (2/2)
        ImageView fileImage = v.findViewById (R.id.file_image);
        ImageView intoFolderLogo = v.findViewById(R.id.into_folder_logo);
        ImageView downloadLogo = v.findViewById(R.id.browser_logo);
        // if is a folder
        if (fileInfo.getDir() == 1) {
            //Change to Folder Imamge
            fileImage.setImageResource(R.drawable.closed22);
            //Hide Download logo
            downloadLogo.setVisibility(View.GONE);
            //set(imitate) jump event when into_folder_logo is clicked
            intoFolderLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)activity).getFileInfoListAndResetAdaptor(fileInfo.getRelativePath());
                }
            });
        } else { // if not a folder
            //Hide IntoFolder logo
            intoFolderLogo.setVisibility(View.GONE);
            //set download event when download_logo is clicked
            downloadLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get user id
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
                    String id = sharedPreferences.getString("id", "");

                    //Construct real url
                    String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
                    String diskRootUrl = baseUrl + "/data/disk/" + id + "/files/";
                    String realUrl = diskRootUrl + fileInfo.getRelativePath();
                    realUrl = realUrl.replace("\\","/");

                    ((MainActivity)activity).downloadInBrowser(realUrl);
                    //((MainActivity)activity).download(realUrl, fileInfo.getName());
                }
            });
        }

        return v;
    }

    private void deleteConfirm(final FileInfo fileInfo) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
        normalDialog.setIcon(R.drawable.opened7);
        normalDialog.setTitle("Confirm Delete");
        normalDialog.setMessage("The deletion cannot recover.");
        normalDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete(fileInfo);
                    }
                });
        normalDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
        normalDialog.show();
    }

    private void inputNewName(final FileInfo fileInfo) {
        final EditText editText = new EditText(activity);

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(activity);
        inputDialog.setTitle("Input New Name").setView(editText);
        inputDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doRename(fileInfo, editText.getText().toString());
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

    private void doDelete(final FileInfo fileInfo){
        //get user id
        SharedPreferences sharedPreferences = activity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
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
                                ((MainActivity)activity).getFileInfoListAndResetAdaptor(((MainActivity)activity).lastRelativePath);
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
        SharedPreferences sharedPreferences = activity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id", "");

        //Use another thread to do server work
        Thread renameRunnable = new Thread() {
            @Override
            public void run() {
                String url = HttpUtilsHttpURLConnection.BASE_URL + "/rename_file";
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("relativePath", ((MainActivity)activity).lastRelativePath);
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
                                ((MainActivity)activity).getFileInfoListAndResetAdaptor(((MainActivity)activity).lastRelativePath);
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}