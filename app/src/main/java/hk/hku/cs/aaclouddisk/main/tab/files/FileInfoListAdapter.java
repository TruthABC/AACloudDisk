package hk.hku.cs.aaclouddisk.main.tab.files;

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

        // set file image logo and click event
        ImageView fileImage = v.findViewById (R.id.file_image);
        ImageView intoFolderLogo = v.findViewById(R.id.into_folder_logo);
        ImageView downLoadLogo = v.findViewById(R.id.download_logo);
        // if is a folder
        if (fileInfo.getDir() == 1) {
            //Change to Folder Imamge
            fileImage.setImageResource(R.drawable.closed22);
            //Hide Download logo
            downLoadLogo.setVisibility(View.GONE);
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
                    //((MainActivity)activity).download(realUrl, fileInfo.getName());
                }
            });
        }

        return v;
    }

}