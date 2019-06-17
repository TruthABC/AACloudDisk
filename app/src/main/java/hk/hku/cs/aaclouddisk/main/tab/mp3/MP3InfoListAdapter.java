package hk.hku.cs.aaclouddisk.main.tab.mp3;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;

public class MP3InfoListAdapter extends ArrayAdapter<FileInfo> {

    private int mResourceId;
    private MainActivity mActivity;

    public MP3InfoListAdapter(Context context, int resourceId, MainActivity activity) {
        super(context, resourceId);
        this.mResourceId = resourceId;
        this.mActivity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final FileInfo fileInfo = getItem(position);
        View v = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);

        // set file name
        TextView fileName = v.findViewById (R.id.file_name);
        fileName.setText(fileInfo.getName());

        //set download event when download_logo is clicked
        ImageView browserLogo = v.findViewById(R.id.browser_logo);
        browserLogo.setOnClickListener((v1) -> {
            mActivity.downloadInBrowser(getRealUrl(fileInfo));
        });

        //set open event when open_logo is clicked
        RelativeLayout rootItem = v.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
//            mActivity.playMusicFile(getRealUrl(fileInfo)); TODO: add to multi select
        });

        return v;
    }

    /**
     * Get real http url by given FileInfo
     * @param fileInfo File Relative Path & Name
     * @return the Real Url of the file
     */
    private String getRealUrl(FileInfo fileInfo) {
        //get user id
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("AACloudLogin", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");

        //Construct real url
        String baseUrl = HttpUtilsHttpURLConnection.BASE_URL;
        String diskRootUrl = baseUrl + "/data/disk/" + id + "/files/";
        String realUrl = diskRootUrl + fileInfo.getRelativePath();
        realUrl = realUrl.replace("\\","/");
        return realUrl;
    }

}
