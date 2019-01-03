package com.cajetan.youtubeplus

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.api.services.youtube.model.Video
import com.squareup.picasso.Picasso

class FavouriteListAdapter(videos: List<Video>, listener: ListItemClickListener, context: Context) :
        RecyclerView.Adapter<FavouriteListAdapter.VideoViewHolder>() {

    private val mVideos: ArrayList<Video> = ArrayList(videos)
    private val mOnClickListener = listener
    private val mContext = context

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.video_list_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(mVideos[position])
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    override fun getItemCount(): Int {
        return mVideos.size
    }

    fun addItems(items: List<Video>) {
        mVideos.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        mVideos.clear()
        notifyDataSetChanged()
    }

    private fun dpToPixel(dp: Float, context: Context): Int {
        val metrics = context.resources.displayMetrics
        return Math.round(dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks & others
    ////////////////////////////////////////////////////////////////////////////////

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener, View.OnLongClickListener {

        private val videoTitleView: TextView = itemView.findViewById(R.id.video_title)
        private val videoChannelView: TextView = itemView.findViewById(R.id.video_author)
        private val videoDurationView: TextView = itemView.findViewById(R.id.video_duration)
        private val videoThumbnailView: ImageView = itemView.findViewById(R.id.video_thumbnail)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(video: Video) {
            videoTitleView.text = video.snippet.title
            videoChannelView.text = video.snippet.channelTitle
            videoThumbnailView.setBackgroundResource(R.color.darkerLightGrey)

            var duration = video.contentDetails.duration

            // TODO: refactor with when
            if (duration == "PT0S") {
                videoDurationView.setBackgroundColor(Color.RED)
                videoDurationView.setTextColor(Color.WHITE)
                videoDurationView.text = mContext.getString(R.string.live)
            } else {
                duration = duration.substring(2, duration.length-1)
                        .replace("M", ":")
                videoDurationView.text = duration
            }

            val thumbnailUrl: String = when {
                video.snippet.thumbnails.maxres != null -> video.snippet.thumbnails.maxres.url
                video.snippet.thumbnails.high != null -> video.snippet.thumbnails.high.url
                video.snippet.thumbnails.medium != null -> video.snippet.thumbnails.medium.url
                video.snippet.thumbnails.standard != null -> video.snippet.thumbnails.standard.url
                video.snippet.thumbnails.default != null -> video.snippet.thumbnails.default.url
                else -> ""
            }

            if (thumbnailUrl != "") {
                Picasso.get().load(thumbnailUrl)
                        .resize(dpToPixel(160f, mContext), dpToPixel(90f, mContext))
                        .centerCrop().into(videoThumbnailView)
            }
        }

        override fun onClick(v: View?) {
            mOnClickListener.onListItemClick(mVideos[adapterPosition].id)
        }

        override fun onLongClick(v: View?): Boolean {
            mOnClickListener.onListItemLongClick(mVideos[adapterPosition].id)
            return true
        }
    }

    interface ListItemClickListener {
        fun onListItemClick(clickedVideoId: String)
        fun onListItemLongClick(clickedVideoId: String)
    }
}
