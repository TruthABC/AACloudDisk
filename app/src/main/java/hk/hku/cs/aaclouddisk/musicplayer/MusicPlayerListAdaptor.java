package hk.hku.cs.aaclouddisk.musicplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import hk.hku.cs.aaclouddisk.R;
import hk.hku.cs.aaclouddisk.entity.musicplayer.ResourceInfo;

public class MusicPlayerListAdaptor extends RecyclerView.Adapter<MusicPlayerListAdaptor.MusicPlayerBodyItemViewHolder> {

    private MusicPlayerActivity mActivity;
    private List<ResourceInfo> mResourceList;
    private MusicService.MusicServiceBinder mMusicServiceBinder;

    public MusicPlayerListAdaptor(MusicPlayerActivity activity, List<ResourceInfo> resourceList) {
        mActivity = activity;
        mResourceList = resourceList;
        mMusicServiceBinder = null;
    }

    @Override
    public MusicPlayerBodyItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_music_player_body_item, parent, false);
        return new MusicPlayerBodyItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicPlayerBodyItemViewHolder holder, int position) {
        String s = mResourceList.get(position).getName();
        holder.mResourceNameTextView.setText(s);
        holder.rootItem.setOnClickListener((v) -> {
            mActivity.showShortToast("[" + s + "]");
            mMusicServiceBinder.jumpTo(position);
        });
    }

    @Override
    public int getItemCount() {
        return mResourceList.size();
    }

    public List<ResourceInfo> getResourceList() {
        return mResourceList;
    }

    public void setMusicServiceBinder(MusicService.MusicServiceBinder musicServiceBinder) {
        mMusicServiceBinder = musicServiceBinder;
    }

    class MusicPlayerBodyItemViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rootItem;
        ImageView frontImage;
        TextView mResourceNameTextView;
        ImageView backImage;
        public MusicPlayerBodyItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rootItem = (RelativeLayout) itemView.findViewById(R.id.root_item);
            frontImage = (ImageView) itemView.findViewById(R.id.front_image);
            mResourceNameTextView = (TextView) itemView.findViewById(R.id.resource_name);
            backImage = (ImageView) itemView.findViewById(R.id.back_image);
        }
    }
}
