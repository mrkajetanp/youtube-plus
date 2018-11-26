package com.cajetan.youtubeplus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {
    private static final String TAG = VideoListAdapter.class.getSimpleName();

    private OnBottomReachedListener onBottomReachedListener;

    private final ListItemClickListener mOnClickListener;
    private List<SearchResult> mVideos;

    public interface ListItemClickListener {
        void onListItemClick(String clickedVideoId);
    }

    public VideoListAdapter(List<SearchResult> videos, ListItemClickListener listener) {
        mOnClickListener = listener;
        mVideos = videos;
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
        holder.bind(mVideos.get(position));

        if (onBottomReachedListener != null && position == mVideos.size() - 1)
            onBottomReachedListener.onBottomReached(position);
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    // TODO: try caching thumbnail Bitmaps

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView videoTitleView;
        TextView videoChannelView;
        ImageView videoThumbnailView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            videoTitleView = itemView.findViewById(R.id.video_title);
            videoChannelView = itemView.findViewById(R.id.video_author);
            videoThumbnailView = itemView.findViewById(R.id.video_thumbnail);

            itemView.setOnClickListener(this);
        }

        void bind(SearchResult video) {
            videoTitleView.setText(video.getSnippet().getTitle());
            videoChannelView.setText(video.getSnippet().getChannelTitle());

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

            if (thumbnailUrl != null)
                new SetThumbnailTask().execute(thumbnailUrl);
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(mVideos.get(getAdapterPosition()).getId().getVideoId());
        }

        private class SetThumbnailTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... strings) {
                Bitmap result = null;

                try {
                    URL url = new URL(strings[0]);
                    result = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                return result;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                videoThumbnailView.setScaleType(ImageView.ScaleType.FIT_XY);
                videoThumbnailView.setImageBitmap(bitmap);
                videoThumbnailView.invalidate();
            }
        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}
