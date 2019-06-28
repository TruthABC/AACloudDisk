package hk.hku.cs.aaclouddisk.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicPlayerBodyListAdaptor extends ArrayAdapter<ResourceInfo> {

    private int mItemResourceId;
    private MusicPlayerActivity mActivity;
    private MusicService.MusicServiceBinder mMusicServiceBinder;

    public MusicPlayerBodyListAdaptor(Context context, int itemResourceId, MusicPlayerActivity activity) {
        super(context,  itemResourceId);
        mItemResourceId = itemResourceId;
        mActivity = activity;
        mMusicServiceBinder = null;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup container){
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(mItemResourceId, container, false);
        }

        ResourceInfo resourceInfo = getItem(position);
        final String name;
        if (resourceInfo != null) {
            name = resourceInfo.getName();
        } else {
            name = "";
        }

        //set resource name
        TextView resourceName = convertView.findViewById(R.id.resource_name);
        resourceName.setText(name);

        //set resource name
        TextView textView = convertView.findViewById(R.id.resource_name);
        ImageView frontImage = convertView.findViewById(R.id.front_image);
        if (mMusicServiceBinder.getNowResourceIndex() == position) {
            textView.setTextColor(mActivity.getResources().getColor(R.color.primary_light));
            frontImage.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(mActivity.getResources().getColor(R.color.white_c));
            frontImage.setVisibility(View.GONE);
        }

        //set open event when open_logo is clicked
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
            mActivity.showShortToast("[" + name + "]");
            mMusicServiceBinder.jumpTo(position);
        });
        return convertView;
    }

    public void setMusicServiceBinder(MusicService.MusicServiceBinder musicServiceBinder) {
        mMusicServiceBinder = musicServiceBinder;
    }
}
