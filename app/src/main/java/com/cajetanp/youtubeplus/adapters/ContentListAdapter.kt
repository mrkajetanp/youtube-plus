package com.cajetanp.youtubeplus.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cajetanp.youtubeplus.R
import com.cajetanp.youtubeplus.utils.FeedItem
import com.cajetanp.youtubeplus.utils.ItemType
import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.Video
import com.squareup.picasso.Picasso

class ContentListAdapter(items: List<FeedItem>, listener: ListItemClickListener, context: Context) :
        RecyclerView.Adapter<ContentListAdapter.VideoViewHolder>() {

    lateinit var onBottomReached: () -> Unit

    var playlistMode: Boolean = false

    private var mItems: ArrayList<FeedItem> = ArrayList(items)
    private val mOnClickListener = listener
    private val mContext = context
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
        when (mItems[position].itemType) {
            ItemType.Video -> {
                holder.bindVideo(mItems[position].video!!)

                // TODO: investigate if unnecessary calls occur
                if (playlistMode) {
                    if (mNowPlaying == position)
                        holder.enableNowPlaying()
                    else
                        holder.disableNowPlaying()
                }
            }

            ItemType.Playlist -> holder.bindPlaylist(mItems[position].playlist!!)
            ItemType.Channel -> holder.bindChannel(mItems[position].channel!!)
        }

        if (position != mItems.size - 1)
            return

        try {
            onBottomReached.invoke()
        } catch (e: UninitializedPropertyAccessException) {
            Log.e("ContentListAdapter", "onBottomReached not set!")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun getItem(index: Int): FeedItem {
        return mItems[index]
    }

    fun addItems(items: List<FeedItem>) {
        mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        mItems.clear()
        notifyDataSetChanged()
    }

    fun setItems(items: ArrayList<FeedItem>) {
        mItems = items
        notifyDataSetChanged()
    }

    fun switchNowPlaying(index: Int) {
        if (index < 0)
            throw IllegalArgumentException("List index cannot be negative")

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
        private val thumbnailView: ImageView = itemView.findViewById(R.id.video_thumbnail)
        private val playlistSizeView: TextView = itemView.findViewById(R.id.playlist_size)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        // TODO: add a frame around the thumbnail
        fun enableNowPlaying() {
            itemView.setBackgroundResource(R.color.lighterDarkGrey)
            videoTitleView.setTextColor(Color.WHITE)
            videoChannelView.setTextColor(Color.WHITE)
            videoTitleView.setTypeface(null, Typeface.BOLD)
            videoChannelView.setTypeface(null, Typeface.BOLD)
        }

        fun disableNowPlaying() {
            itemView.setBackgroundResource(R.color.darkGrey)
            videoTitleView.setTextColor(Color.WHITE)
            videoChannelView.setTextColor(Color.WHITE)
            videoTitleView.setTypeface(null, Typeface.NORMAL)
            videoChannelView.setTypeface(null, Typeface.NORMAL)
        }

        fun bindVideo(video: Video) {
            videoDurationView.visibility = View.VISIBLE
            playlistSizeView.visibility = View.GONE

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
                        .centerCrop().into(thumbnailView)
            }
        }

        fun bindPlaylist(playlist: Playlist) {
            videoDurationView.visibility = View.GONE
            playlistSizeView.visibility = View.VISIBLE

            videoTitleView.text = playlist.snippet.title
            videoChannelView.text = playlist.snippet.channelTitle
            playlistSizeView.text = mContext.getString(R.string.number_videos, playlist.contentDetails.itemCount)

            val thumbnailUrl: String = when {
                playlist.snippet.thumbnails.maxres != null ->
                    playlist.snippet.thumbnails.maxres.url
                playlist.snippet.thumbnails.high != null ->
                    playlist.snippet.thumbnails.high.url
                playlist.snippet.thumbnails.medium != null ->
                    playlist.snippet.thumbnails.medium.url
                playlist.snippet.thumbnails.standard != null ->
                    playlist.snippet.thumbnails.standard.url
                playlist.snippet.thumbnails.default != null ->
                    playlist.snippet.thumbnails.default.url
                else -> ""
            }

            if (thumbnailUrl.isNotEmpty()) {
                Picasso.get().load(thumbnailUrl)
                        .resize(dpToPixel(160f, mContext), dpToPixel(90f, mContext))
                        .centerCrop().into(thumbnailView)
            }
        }

        fun bindChannel(channel: Channel) {
            videoDurationView.visibility = View.GONE
            playlistSizeView.visibility = View.VISIBLE

            videoTitleView.text = channel.snippet.title
            videoChannelView.text = mContext.getString(R.string.number_subscribers, channel.statistics.subscriberCount)
            playlistSizeView.text = mContext.getString(R.string.number_videos, channel.statistics.videoCount)

            val thumbnailUrl: String = when {
                channel.snippet.thumbnails.maxres != null ->
                    channel.snippet.thumbnails.maxres.url
                channel.snippet.thumbnails.high != null ->
                    channel.snippet.thumbnails.high.url
                channel.snippet.thumbnails.medium != null ->
                    channel.snippet.thumbnails.medium.url
                channel.snippet.thumbnails.standard != null ->
                    channel.snippet.thumbnails.standard.url
                channel.snippet.thumbnails.default != null ->
                    channel.snippet.thumbnails.default.url
                else -> ""
            }

            if (thumbnailUrl.isNotEmpty()) {
                Picasso.get().load(thumbnailUrl)
                        .resize(dpToPixel(160f, mContext), dpToPixel(90f, mContext))
                        .centerCrop().into(thumbnailView)
            }
        }

        override fun onClick(v: View?) {
            mOnClickListener.onListItemClick(mItems[adapterPosition].id, adapterPosition,
                    mItems[adapterPosition].itemType)
        }

        override fun onLongClick(v: View?): Boolean {
            mOnClickListener.onListItemLongClick(mItems[adapterPosition].id,
                    mItems[adapterPosition].itemType)

            return true
        }
    }

    interface ListItemClickListener {
        fun onListItemClick(id: String, position: Int, type: ItemType)
        fun onListItemLongClick(id: String, type: ItemType)
    }
}
