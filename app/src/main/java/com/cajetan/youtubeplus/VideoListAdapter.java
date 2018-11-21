package com.cajetan.youtubeplus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.youtube.model.SearchResult;

import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {
    private static final String TAG = VideoListAdapter.class.getSimpleName();

    private final ListItemClickListener mOnClickListener;
    private List<SearchResult> mVideos;

    public interface ListItemClickListener {
        void onListItemClick(String clickedVideoId);
    }

    public VideoListAdapter(List<SearchResult> videos, ListItemClickListener listener) {
        mOnClickListener = listener;
        mVideos = videos;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.video_list_item, parent, false);
        VideoViewHolder holder = new VideoViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.bind(mVideos.get(position));
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView videoTitleView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            videoTitleView = itemView.findViewById(R.id.video_title);

            itemView.setOnClickListener(this);
        }

        void bind(SearchResult video) {
            videoTitleView.setText(video.getSnippet().getTitle());
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(mVideos.get(getAdapterPosition()).getId().getVideoId());
        }
    }
}
