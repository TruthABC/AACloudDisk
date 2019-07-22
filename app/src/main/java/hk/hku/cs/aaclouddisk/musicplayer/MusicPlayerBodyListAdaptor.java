package hk.hku.cs.aaclouddisk.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicPlayerBodyListAdaptor extends ArrayAdapter<ResourceInfo> {

    private int mItemResourceId;
    private MusicPlayerActivity mActivity;

    public MusicPlayerBodyListAdaptor(Context context, int itemResourceId, MusicPlayerActivity activity) {
        super(context,  itemResourceId);
        mItemResourceId = itemResourceId;
        mActivity = activity;
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
        if (mActivity.mMusicServiceBinder.getNowResourceIndex() == position) {
            textView.setTextColor(mActivity.getResources().getColor(R.color.primary_light));
            frontImage.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(mActivity.getResources().getColor(R.color.white_c));
            frontImage.setVisibility(View.GONE);
        }

        //set event when root is clicked
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
            mActivity.showShortToast("[" + name + "]");
            mActivity.mMusicServiceBinder.jumpTo(position);
        });

        //set event when remove is clicked
        ImageView removeIcon = convertView.findViewById(R.id.back_image);
        //if (isOnlineMusicList) cannot delete
        removeIcon.setOnClickListener((v) -> {
            if (mActivity.mMusicServiceBinder.getResourceList().size() <= 1) {
                mActivity.showShortToast("Cannot remove the last song.");
            } else {
                showRemoveMusicFromListConfirm(position, mActivity.mMusicListIndex);
            }
        });

        return convertView;
    }

    private void showRemoveMusicFromListConfirm(int musicIndex, int listIndex) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Remove Music");
        normalDialog.setMessage("File will not be deleted.");
        normalDialog.setPositiveButton("Confirm", (dialog, which) -> {
            doRemoveMusicFromList(musicIndex, listIndex);
        });
        normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        normalDialog.show();
    }

    private void doRemoveMusicFromList(int musicIndex, int listIndex) {
        //clear history played
        mActivity.mMusicServiceBinder.clearHistory();
        int playingIndex =  mActivity.mMusicServiceBinder.getNowResourceIndex();

        //Remove
        List<ResourceInfo> musicPlayerResourceList = mActivity.mMusicServiceBinder.getResourceList();
        List<ResourceInfo> musicListsResourceList = mActivity.mMusicListServiceBinder.getMusicLists().get(listIndex).getResourceList();
        //Remove in ListView
        mActivity.mPlayerBodyListAdaptor.remove(musicPlayerResourceList.get(musicIndex));
        mActivity.mPlayerBodyListAdaptor.notifyDataSetChanged();
        //Remove in Service Data Structure
        musicPlayerResourceList.remove(musicIndex);
        if (musicListsResourceList != musicPlayerResourceList) {//Bug-fix: In case double delete on the same list reference
            musicListsResourceList.remove(musicIndex);
        }
        mActivity.mPlayerBottomListAdaptor.notifyDataSetChanged();
        mActivity.mMusicListServiceBinder.saveMusicLists();
        if (playingIndex == musicIndex) {
            if (playingIndex > 0) {
                playingIndex--;
            } else {
                playingIndex = 0;
            }
            mActivity.mMusicServiceBinder.jumpTo(playingIndex);
        } else {
            //do nothing
        }
    }
}
