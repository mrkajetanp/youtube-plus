package com.cajetan.youtubeplus

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
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

    fun filterItems(query: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            mVideos.removeIf { !it.snippet.title.toLowerCase().contains(query.toLowerCase()) }
        notifyDataSetChanged()

//        TODO: implement functionality for older versions
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

        fun bind(video: Video) {
            videoTitleView.text = video.snippet.title
            videoChannelView.text = video.snippet.channelTitle
            videoThumbnailView.setBackgroundResource(R.color.darkerLightGrey)

            val duration = video.contentDetails.duration
            videoDurationView.text = when (duration) {
                "PT0S" -> {
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
