package com.cajetan.youtubeplus.adapters

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cajetan.youtubeplus.R
import com.google.api.services.youtube.model.Video
import com.squareup.picasso.Picasso

class VideoListAdapter(videos: List<Video>, listener: ListItemClickListener, context: Context) :
        RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    lateinit var onBottomReached: () -> Unit

    private val mVideos: ArrayList<Video> = ArrayList(videos)
    private val mOnClickListener = listener
    private val mContext = context
    // Non-negative value means the adapter is showing a playlist
    private var mNowPlaying: Int = -1

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_video_list, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(mVideos[position])

        // TODO: investigate if unnecessary calls occur
        // Only switch those in "playlist mode
        if (mNowPlaying != -1) {
            if (mNowPlaying == position)
                holder.enableNowPlaying()
            else
                holder.disableNowPlaying()
        }

        if (position != mVideos.size - 1)
            return

        try {
            onBottomReached.invoke()
        } catch (e: UninitializedPropertyAccessException) {
            Log.e("VideoListAdapter", "onBottomReached not set!")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    override fun getItemCount(): Int {
        return mVideos.size
    }

    fun getItem(index: Int): Video {
        return mVideos[index]
    }

    fun addItems(items: List<Video>) {
        mVideos.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        mVideos.clear()
        notifyDataSetChanged()
    }

    fun switchNowPlaying(index: Int) {
        // TODO: ensure the index is positive
        if (index == 0) {
            // Going into playlist mode, refresh all items
            notifyDataSetChanged()
        } else {
            // Already in playlist mode, refresh only relevant items
            // Refresh the new item
            notifyItemChanged(index)
            // Refresh the previously played item
            notifyItemChanged(mNowPlaying)
        }

        mNowPlaying = index
    }

    private fun dpToPixel(dp: Float, context: Context): Int {
        val metrics = context.resources.displayMetrics
        return Math.round(dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private fun parseDuration(input: String): String {
        var result = input.filterNot { it == 'P' || it == 'T' }

        var hours: Int = when {
            result.contains('D') -> {
                val temp = result.takeWhile { it != 'D' }.toInt() * 24
                result = result.dropWhile { it != 'D' }.filterNot { it == 'D' }
                temp
            }
            else -> 0
        }

        hours += when {
            result.contains('H') -> {
                val temp = result.takeWhile { it != 'H' }.toInt()
                result = result.dropWhile { it != 'H' }.filterNot { it == 'H' }
                temp
            }
            else -> 0
        }

        val minutes: Int = when {
            result.contains('M') -> {
                val temp = result.takeWhile { it != 'M' }.toInt()
                result = result.dropWhile { it != 'M' }.filterNot { it == 'M' }
                temp
            }
            else -> 0
        }

        val seconds: Int = when {
            result.contains('S') -> result.takeWhile { it != 'S' }.toInt()
            else -> 0
        }

        return when (hours) {
            0 -> "$minutes:${String.format("%02d", seconds)}"
            else -> "$hours:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        }
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

        // TODO: add a frame around the thumbnail and make text bold
        fun enableNowPlaying() {
            itemView.setBackgroundResource(R.color.lighterDarkGrey)
            videoTitleView.setTextColor(Color.WHITE)
            videoChannelView.setTextColor(Color.WHITE)
        }

        fun disableNowPlaying() {
            itemView.setBackgroundResource(R.color.darkGrey)
            videoTitleView.setTextColor(Color.WHITE)
            videoChannelView.setTextColor(Color.WHITE)
        }

        fun bind(video: Video) {
            Log.d("VideoListAdapter", "Now playing $mNowPlaying")
            if (mNowPlaying != -1) {
                itemView.setBackgroundResource(R.color.darkGrey)
                videoTitleView.setTextColor(Color.WHITE)
                videoChannelView.setTextColor(Color.WHITE)
            }

            videoTitleView.text = video.snippet.title
            videoChannelView.text = video.snippet.channelTitle

            val duration = video.contentDetails.duration
            videoDurationView.text = when (duration) {
                mContext.getString(R.string.live_video_duration) -> {
                    videoDurationView.setBackgroundColor(Color.RED)
                    videoDurationView.setTextColor(Color.WHITE)
                    mContext.getString(R.string.live)
                }
                else -> parseDuration(duration)
            }

            val thumbnailUrl: String = when {
                video.snippet.thumbnails.maxres != null -> video.snippet.thumbnails.maxres.url
                video.snippet.thumbnails.high != null -> video.snippet.thumbnails.high.url
                video.snippet.thumbnails.medium != null -> video.snippet.thumbnails.medium.url
                video.snippet.thumbnails.standard != null -> video.snippet.thumbnails.standard.url
                video.snippet.thumbnails.default != null -> video.snippet.thumbnails.default.url
                else -> ""
            }

            if (thumbnailUrl.isNotEmpty()) {
                Picasso.get().load(thumbnailUrl)
                        .resize(dpToPixel(160f, mContext), dpToPixel(90f, mContext))
                        .centerCrop().into(videoThumbnailView)
            }
        }

        override fun onClick(v: View?) {
            mOnClickListener.onListItemClick(mVideos[adapterPosition].id, adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            mOnClickListener.onListItemLongClick(mVideos[adapterPosition].id)
            return true
        }
    }

    interface ListItemClickListener {
        fun onListItemClick(clickedVideoId: String, position: Int)
        fun onListItemLongClick(clickedVideoId: String)
    }
}
