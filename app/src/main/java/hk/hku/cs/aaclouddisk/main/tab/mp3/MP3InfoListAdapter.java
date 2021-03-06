package hk.hku.cs.aaclouddisk.main.tab.mp3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.response.FileInfo;

public class MP3InfoListAdapter extends ArrayAdapter<FileInfo> {

    private int mResourceId;
    private MainActivity mActivity;

    public MP3InfoListAdapter(Context context, int itemResourceId, MainActivity activity) {
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

        //set file name
        TextView fileName = convertView.findViewById (R.id.file_name);
        fileName.setText(fileInfo.getName());

        //set event when add_to_list is clicked
        ImageView addToListLogo = convertView.findViewById(R.id.add_to_list_logo);
        addToListLogo.setOnClickListener((v1) -> {
            mActivity.clickedMusicIndex = position;
            mActivity.mTabPagerAdapter.getMP3Fragment().showBottom();
        });

        //set event when root is clicked
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
            mActivity.showShortToast("[" + fileInfo.getName() + "]");
            //set default Online-All List and Play
            mActivity.mMusicServiceBinder.setResourceListByMusicList(mActivity.mMusicListServiceBinder.getMusicLists().get(0), 0);
            mActivity.mMusicServiceBinder.jumpTo(position);
        });
        return convertView;
    }
}
