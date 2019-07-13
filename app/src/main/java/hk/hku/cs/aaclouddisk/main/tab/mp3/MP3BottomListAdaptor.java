package hk.hku.cs.aaclouddisk.main.tab.mp3;

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

import hk.hku.cs.aaclouddisk.MainActivity;
import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.MusicList;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MP3BottomListAdaptor extends ArrayAdapter<MusicList> {

    private int mItemResourceId;
    private MainActivity mActivity;

    public MP3BottomListAdaptor(Context context, int itemResourceId, MainActivity activity) {
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
        if (position == 0) {
            deleteListLogo.setVisibility(View.INVISIBLE);
            renameListLogo.setVisibility(View.INVISIBLE);
            deleteListLogo.setOnClickListener(null);
            renameListLogo.setOnClickListener(null);

        } else {
            deleteListLogo.setVisibility(View.INVISIBLE);
            renameListLogo.setVisibility(View.INVISIBLE);
            deleteListLogo.setOnClickListener((v) -> {
                deleteConfirm(position);
            });
            renameListLogo.setOnClickListener((v) -> {
                inputNewName(position);
            });
        }

        //set open event when open_logo is clicked
        RelativeLayout rootItem = convertView.findViewById(R.id.root_item);
        rootItem.setOnClickListener((v1) -> {
            if (mActivity.clickedMusicIndex != -1) {
                ResourceInfo resourceInfo = mActivity.mMusicListServiceBinder.getMusicLists().get(0).getResourceList().get(mActivity.clickedMusicIndex);
                mActivity.mMusicListServiceBinder.addMusicToList(resourceInfo, position);
                mActivity.mMusicListServiceBinder.saveMusicLists();
                mActivity.showShortToast("Music Added to List");
                notifyDataSetChanged();
                mActivity.mTabPagerAdapter.getMP3Fragment().hideBottom();
            } else {
                mActivity.showShortToast("Failed Add to List");
            }
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
                this.remove(this.getItem(position));
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

        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(mActivity);
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
