package com.cajetan.youtubeplus;

import android.support.v7.widget.RecyclerView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// TODO: change ViewHolder colour onClick

public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.VideoViewHolder> {
    private static final String TAG = FavouriteListAdapter.class.getSimpleName();

    private final ListItemClickListener mOnClickListener;
    private ArrayList<Video> mVideos;
    private Context context;

    /*//////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////////////////////*/

    FavouriteListAdapter(List<Video> videos, ListItemClickListener listener, Context context) {
        mOnClickListener = listener;
        mVideos = new ArrayList<>(videos);
        this.context = context;
    }

    @Override
    @NonNull
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.video_list_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(mVideos.get(position));
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////////*/

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public void addItems(List<Video> items) {
        this.mVideos.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        this.mVideos = new ArrayList<>();
        notifyDataSetChanged();
    }

    private static int dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Callbacks & others
    //////////////////////////////////////////////////////////////////////////////*/

    class VideoViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        TextView videoTitleView;
        TextView videoChannelView;
        TextView videoDurationView;
        ImageView videoThumbnailView;

        VideoViewHolder(View itemView) {
            super(itemView);

            videoTitleView = itemView.findViewById(R.id.video_title);
            videoChannelView = itemView.findViewById(R.id.video_author);
            videoThumbnailView = itemView.findViewById(R.id.video_thumbnail);
            videoDurationView = itemView.findViewById(R.id.video_duration);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bind(Video video) {
            videoTitleView.setText(video.getSnippet().getTitle());
            videoChannelView.setText(video.getSnippet().getChannelTitle());
            videoThumbnailView.setBackgroundColor(Color.parseColor("#e5e5e5"));

            String duration = video.getContentDetails().getDuration();

            // Show duration in HH:MM:SS or 'LIVE' accordingly
            if (duration.equals("PT0S")) {
                videoDurationView.setBackgroundColor(Color.RED);
                videoDurationView.setTextColor(Color.WHITE);
                videoDurationView.setText("LIVE");
            } else {
                duration = duration.substring(2, duration.length() - 1);
                duration = duration.replace("M", ":");
                videoDurationView.setText(duration);
            }

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
            mOnClickListener.onListItemClick(mVideos.get(getAdapterPosition()).getId());
        }

        @Override
        public boolean onLongClick(View v) {
            mOnClickListener.onListItemLongClick(mVideos.get(getAdapterPosition()).getId());
            return true;
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(String clickedVideoId);
        void onListItemLongClick(String clickedVideoId);
    }
}
