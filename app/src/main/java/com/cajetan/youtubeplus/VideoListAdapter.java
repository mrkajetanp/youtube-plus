package com.cajetan.youtubeplus;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {
    private static final String TAG = VideoListAdapter.class.getSimpleName();

    private OnBottomReachedListener onBottomReachedListener;

    private final ListItemClickListener mOnClickListener;
    private ArrayList<SearchResult> mVideos;
    private Context context;

    private int currentPosition = 0;

    public interface ListItemClickListener {
        void onListItemClick(String clickedVideoId);
    }

    public VideoListAdapter(List<SearchResult> videos, ListItemClickListener listener, Context context) {
        mOnClickListener = listener;
        mVideos = new ArrayList<>(videos);
        this.context = context;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
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
        this.currentPosition = position;

        holder.bind(mVideos.get(position));

        if (onBottomReachedListener != null && position == mVideos.size() - 1)
            onBottomReachedListener.onBottomReached(position);
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public void addItems(List<SearchResult> items) {
        this.mVideos.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        this.mVideos = new ArrayList<>();
        notifyDataSetChanged();
    }

    // TODO: try caching thumbnail Bitmaps

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView videoTitleView;
        TextView videoChannelView;
//        TextView videoDurationView;
        ImageView videoThumbnailView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            videoTitleView = itemView.findViewById(R.id.video_title);
            videoChannelView = itemView.findViewById(R.id.video_author);
            videoThumbnailView = itemView.findViewById(R.id.video_thumbnail);
//            videoDurationView = itemView.findViewById(R.id.video_duration);

            itemView.setOnClickListener(this);
        }

        void bind(SearchResult video) {
            videoTitleView.setText(video.getSnippet().getTitle());
            videoChannelView.setText(video.getSnippet().getChannelTitle());
            videoThumbnailView.setBackgroundColor(Color.parseColor("#e5e5e5"));

            String thumbnailUrl = null;

            if (video.getSnippet().getThumbnails().getMaxres() != null)
                thumbnailUrl = video.getSnippet().getThumbnails().getMaxres().getUrl();
            else if (video.getSnippet().getThumbnails().getHigh() != null)
                thumbnailUrl = video.getSnippet().getThumbnails().getHigh().getUrl();
            else if (video.getSnippet().getThumbnails().getMedium() != null)
                thumbnailUrl = video.getSnippet().getThumbnails().getMedium().getUrl();
            else if (video.getSnippet().getThumbnails().getStandard() != null)
                thumbnailUrl = video.getSnippet().getThumbnails().getStandard().getUrl();
            else if (video.getSnippet().getThumbnails().getDefault() != null)
                thumbnailUrl = video.getSnippet().getThumbnails().getDefault().getUrl();

            if (thumbnailUrl != null) {
                Picasso.get().load(thumbnailUrl)
                        .resize(dpToPixel(160, context), dpToPixel(90, context))
                        .centerCrop().into(videoThumbnailView);
            }
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(mVideos.get(getAdapterPosition()).getId().getVideoId());
        }
    }

    public static int dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}
