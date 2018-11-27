package hk.hku.cs.aaclouddisk.main.tab.files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;

public class FileInfoListAdapter extends ArrayAdapter<FileInfo> {

    private int resourceId;

    public FileInfoListAdapter(Context context, int resourceId) {
        super(context, resourceId);
        this.resourceId = resourceId;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        FileInfo fileInfo = getItem(position);
        View v = LayoutInflater.from (getContext()).inflate (resourceId, parent, false);

        // set file name
        TextView fileName = v.findViewById (R.id.file_name);
        fileName.setText(fileInfo.getName());

        // set file image logo
        ImageView fileImage = v.findViewById (R.id.file_image);
        if (fileInfo.getDir() == 1) {
            fileImage.setImageResource(R.drawable.closed22);
        } else {
            v.findViewById(R.id.into_folder_logo).setVisibility(View.INVISIBLE);
        }

        return v;
    }

}