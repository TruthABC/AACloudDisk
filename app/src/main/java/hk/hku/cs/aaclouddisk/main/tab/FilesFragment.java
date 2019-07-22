package hk.hku.cs.aaclouddisk.main.tab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URLEncoder;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.main.tab.files.FileInfoListAdapter;

public class FilesFragment extends Fragment {

    //Tag
    public static final String TAG = "FilesFragment";
    public static final String DEBUG_TAG = "shijian";

    //Context: Parent Activity
    public MainActivity mActivity;

    //Views
    private ImageView mBackImageView;
    public TextView mPathTextView;
    private ImageView mUploadFileImageView;

    private TextView mCreateNewFolder;

    private ListView mFileList;
    public FileInfoListAdapter mFileListAdaptor;

    public TextView mNoFileHint;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_files, container, false);

        mActivity = (MainActivity) getActivity();

        initViews(rootView);
        initEvents();
        initFinal();

        return rootView;
    }

    private void initViews(View v) {
        mBackImageView = (ImageView) v.findViewById(R.id.tab_files_back);
        mPathTextView = (TextView) v.findViewById(R.id.tab_files_title);
        mUploadFileImageView = (ImageView) v.findViewById(R.id.tab_files_uploadFile);

        mCreateNewFolder = (TextView) v.findViewById(R.id.new_folder_text);

        mFileListAdaptor = new FileInfoListAdapter(getContext(), R.layout.tab_files_item, mActivity);
        mFileList = (ListView) v.findViewById(R.id.list_view_files);
        mFileList.setAdapter(mFileListAdaptor);

        mNoFileHint = (TextView) v.findViewById(R.id.no_file_hint);
    }

    private void initEvents() {
        //Event - back
        mBackImageView.setOnClickListener((v) -> {
            if (mActivity.lastRelativePath.length()==0) {
                mActivity.showShortToast("Cannot Go Back More");
                return;
            }
            //"lastIndex" to check if it is jump with relative path ""
            String nextPath = "";
            int lastIndex = mActivity.lastRelativePath.lastIndexOf("\\");
            if (lastIndex > 0) {
                nextPath = mActivity.lastRelativePath.substring(0, lastIndex);
            }
            //back to last level
            mActivity.getFileInfoListAndResetAdaptor(nextPath);
        });
        mUploadFileImageView.setOnClickListener((v) -> {
            //go to upload file web page TODO: could be web view
            String iii = HttpUtilsHttpURLConnection.BASE_URL + "/upload_file.html?id=" + URLEncoder.encode(mActivity.userId) + "&relativePath=" + URLEncoder.encode(mActivity.lastRelativePath);
            Uri uri = Uri.parse(iii);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        mCreateNewFolder.setOnClickListener((v) -> {
            mActivity.createFolderAndHandle();
        });
    }

    private void initFinal() {
        mActivity.getFileInfoListAndResetAdaptor(mActivity.lastRelativePath);
    }
}
