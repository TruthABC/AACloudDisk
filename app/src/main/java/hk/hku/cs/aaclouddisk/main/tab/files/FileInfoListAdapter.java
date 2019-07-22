package hk.hku.cs.aaclouddisk.main.tab.files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
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
        ImageView fileImage = convertView.findViewById (R.id.file_image);
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        if (fileInfo.getDir() == 1) { // if is a folder
            fileImage.setImageResource(R.drawable.closed22); //Change to Folder Image
            rootItem.setOnClickListener((v) -> {
                Thread waitOneSecondAndGoIntoFolder = new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mActivity.getFileInfoListAndResetAdaptor(fileInfo.getRelativePath());
                });
                waitOneSecondAndGoIntoFolder.start();
            });
        } else { // if not a folder
            fileImage.setImageResource(R.drawable.file77); //Change to File Image
            rootItem.setOnClickListener((v) -> {
                //Construct real url
                String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
                String diskRootUrl = baseUrl + "/data/disk/" + mActivity.userId + "/files/";
                String realUrl = diskRootUrl + fileInfo.getRelativePath();
                realUrl = realUrl.replace("\\","/");

                mActivity.downloadInBrowser(realUrl);
            });
        }

        // set file or folder image logo and click event (2/2)
        ImageView deleteLogo = convertView.findViewById(R.id.delete_logo);
        ImageView renameLogo = convertView.findViewById(R.id.rename_logo);
        deleteLogo.setOnClickListener((v) -> {
            deleteConfirm(fileInfo);
        });
        renameLogo.setOnClickListener((v) -> {
            inputNewName(fileInfo);
        });

        return convertView;
    }

    private void deleteConfirm(FileInfo fileInfo) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Confirm Delete");
        normalDialog.setMessage("The deletion cannot recover.");
        normalDialog.setPositiveButton("Confirm", (dialog, which) -> {
            mActivity.deleteFileAndHandle(fileInfo);
        });
        normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        normalDialog.show();
    }

    private void inputNewName(FileInfo fileInfo) {
        final EditText editText = new EditText(mActivity);
        editText.setText(fileInfo.getName());

        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mActivity);
        inputDialog.setTitle("Input New Name").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            mActivity.renameFileAndHandle(fileInfo, editText.getText().toString());
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }
}