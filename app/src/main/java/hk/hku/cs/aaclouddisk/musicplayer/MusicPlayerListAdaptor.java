package hk.hku.cs.aaclouddisk.musicplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import hk.hku.cs.aaclouddisk.R;

public class MusicPlayerListAdaptor extends RecyclerView.Adapter<MusicPlayerListAdaptor.MusicPlayerBodyItemViewHolder> {

    private MusicPlayerActivity mActivity;
    private List<String> mResourceList;

    public MusicPlayerListAdaptor(MusicPlayerActivity activity, List<String> resourceList) {
        mActivity = activity;
        mResourceList = resourceList;
    }

    @Override
    public MusicPlayerBodyItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_music_player_body_item, parent, false);
        return new MusicPlayerBodyItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicPlayerBodyItemViewHolder holder, int position) {
        String s = mResourceList.get(position);
        holder.mResourceNameTextView.setText(s);
        holder.rootItem.setOnClickListener((v) -> {
            mActivity.showShortToast("[item onclick TODO]");
        });
    }

    @Override
    public int getItemCount() {
        return mResourceList.size();
    }

    public List<String> getResourceList() {
        return mResourceList;
    }

    class MusicPlayerBodyItemViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rootItem;
        TextView mResourceNameTextView;
        public MusicPlayerBodyItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rootItem = (RelativeLayout) itemView.findViewById(R.id.root_item);
            mResourceNameTextView = (TextView) itemView.findViewById(R.id.resource_name);
        }
    }

}
