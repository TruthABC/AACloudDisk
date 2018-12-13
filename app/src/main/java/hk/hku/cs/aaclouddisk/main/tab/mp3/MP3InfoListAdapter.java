package hk.hku.cs.aaclouddisk.main.tab.mp3;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.HttpUtilsHttpURLConnection;
import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;

public class MP3InfoListAdapter extends ArrayAdapter<FileInfo> {

    private int resourceId;
    private AppCompatActivity activity;

    public MP3InfoListAdapter(Context context, int resourceId, AppCompatActivity activity) {
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

        //set download event when download_logo is clicked
        ImageView downLoadLogo = v.findViewById(R.id.download_logo);
        downLoadLogo.setOnClickListener(new View.OnClickListener() {
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
            }
        });

        return v;
    }

}
