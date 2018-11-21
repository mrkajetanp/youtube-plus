package com.cajetan.youtubeplus;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {
    private static final String TAG = VideoListAdapter.class.getSimpleName();

    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public VideoListAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public VideoViewHolder(View itemView) {
            super(itemView);
        }

        void bind() {

        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
