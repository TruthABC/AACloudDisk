package hk.hku.cs.aaclouddisk.musicplayer;

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


import java.util.List;

import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.MusicList;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicPlayerBottomListAdaptor extends ArrayAdapter<MusicList> {

    private int mItemResourceId;
    private MusicPlayerActivity mActivity;

    public MusicPlayerBottomListAdaptor(Context context, int itemResourceId, MusicPlayerActivity activity) {
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

        MusicList musicList = getItem(position);
        final String name;
        int musicCount = 0;
        if (musicList != null) {
            name = musicList.getListName();
            musicCount = musicList.getResourceList().size(); // TODO worry: if no any music (a new account), will it be problems with music list (all music online)
        } else {
            name = "";
        }

        //set resource name
        TextView listName = convertView.findViewById(R.id.list_name);
        listName.setText(name + " (" + musicCount + ")");

        //set visibility (invisible for default online list)
        ImageView deleteListLogo = convertView.findViewById(R.id.delete_list_logo);
        ImageView renameListLogo = convertView.findViewById(R.id.rename_list_logo);
        if (position == 0 || position == mActivity.mMusicServiceBinder.getNowMusicListIndex()) {
            deleteListLogo.setImageResource(R.drawable.round_volume_up_black_36);
            deleteListLogo.setVisibility(View.INVISIBLE);
            renameListLogo.setVisibility(View.INVISIBLE);
            deleteListLogo.setOnClickListener(null);
            renameListLogo.setOnClickListener(null);
            if (position == mActivity.mMusicServiceBinder.getNowMusicListIndex()) {
                deleteListLogo.setVisibility(View.VISIBLE);
            }
        } else {
            deleteListLogo.setImageResource(R.drawable.round_delete_forever_black_36);
            deleteListLogo.setVisibility(View.VISIBLE);
            renameListLogo.setVisibility(View.VISIBLE);
            deleteListLogo.setOnClickListener((v) -> {
                deleteConfirm(position);
            });
            renameListLogo.setOnClickListener((v) -> {
                inputNewName(position);
            });
        }

        //set set ResourceList event when rootItem is clicked
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
            List<ResourceInfo> resourceList = mActivity.mMusicListServiceBinder.getMusicLists().get(position).getResourceList();
            if (resourceList != null && resourceList.size() > 0) {
                mActivity.mPlayerBodyListAdaptor.clear();
                mActivity.mPlayerBodyListAdaptor.addAll(resourceList);
                mActivity.mPlayerBodyListAdaptor.notifyDataSetChanged();
                mActivity.mMusicServiceBinder.setResourceListByMusicList(mActivity.mMusicListServiceBinder.getMusicLists().get(position), position);
                mActivity.mMusicServiceBinder.play();
            } else {
                mActivity.showShortToast("Cannot Play Empty List");
            }
            this.notifyDataSetChanged();
        });
        return convertView;
    }

    private void deleteConfirm(int position) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setIcon(R.drawable.round_delete_forever_black_36);
        normalDialog.setTitle("Confirm Delete");
        normalDialog.setMessage("The deletion cannot recover.");
        normalDialog.setPositiveButton("Confirm", (dialog, which) -> {
            if (mActivity.mMusicListServiceBinder != null) {
                this.remove(this.getItem(position)); //will auto-call notifyDataSetChanged in this method
                mActivity.mMusicListServiceBinder.removeMusicList(position);
                mActivity.mMusicListServiceBinder.saveMusicLists();
            } else {
                mActivity.showShortToast("Delete Failed.");
            }
        });
        normalDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        normalDialog.show();
    }

    private void inputNewName(int position) {
        final EditText editText = new EditText(mActivity);
        editText.setText(this.getItem(position).getListName());

        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mActivity);
        inputDialog.setTitle("Input New Name").setView(editText);
        inputDialog.setPositiveButton("Confirm", (dialog, which) -> {
            if (mActivity.mMusicListServiceBinder != null) {
                String newName = editText.getText().toString();
                this.getItem(position).setListName(newName);
                this.notifyDataSetChanged();
                mActivity.mMusicListServiceBinder.getMusicLists().get(position).setListName(newName);
                mActivity.mMusicListServiceBinder.saveMusicLists();
            } else {
                mActivity.showShortToast("Rename Failed.");
            }
        });
        inputDialog.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });
        inputDialog.show();
    }
}
