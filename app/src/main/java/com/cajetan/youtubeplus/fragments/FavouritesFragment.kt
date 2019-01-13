package com.cajetan.youtubeplus.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cajetan.youtubeplus.FavouriteListAdapter
import com.cajetan.youtubeplus.PlayerActivity
import com.cajetan.youtubeplus.R
import com.cajetan.youtubeplus.VideoListAdapter
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.VideoDataViewModel
import com.cajetan.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.Video
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class FavouritesFragment : Fragment(), FavouriteListAdapter.ListItemClickListener,
        YouTubeData.FavouritesDataListener {

    private lateinit var mAdapter: FavouriteListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mVideoDataViewModel: VideoDataViewModel

    private lateinit var videoList: RecyclerView
    private lateinit var progressBarCentre: ProgressBar
    private lateinit var noFavouritesView: TextView

    // TODO: caching results?

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDatabase()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter = FavouriteListAdapter(emptyList(), this, activity!!)
        mYouTubeData = YouTubeData(activity!!, this)

        setupFavouritesList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.favourites_fragment, container, false)

        videoList = view.findViewById(R.id.videoList)
        progressBarCentre = view.findViewById(R.id.progressBarCentre)
        noFavouritesView = view.findViewById(R.id.noFavouritesView)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupFavouritesList() {
        videoList.layoutManager = LinearLayoutManager(activity!!)
        videoList.setHasFixedSize(false)
        videoList.adapter = mAdapter
    }

    private fun setupDatabase() {
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
        mVideoDataViewModel.getAllVideoData().observe(this, Observer {
            if (it != null)
                loadFavourites(it)
        })
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    fun filterVideos(query: String) {
        if (query.isNotEmpty())
            mAdapter.filterItems(query)
    }

    private fun loadFavourites(videoData: List<VideoData>) {
        videoList.visibility = View.INVISIBLE
        progressBarCentre.visibility = View.VISIBLE

        mYouTubeData.receiveFavouritesResults(videoData)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onFavouritesReceived(results: List<Video>) {
        mAdapter.clearItems()
        mAdapter.addItems(results.toList())

        noFavouritesView.visibility = if (mAdapter.itemCount == 0) View.VISIBLE else View.GONE
        progressBarCentre.visibility = View.INVISIBLE
        videoList.visibility = View.VISIBLE
    }

    override fun onListItemClick(clickedVideoId: String) {
        startActivity(activity!!.intentFor<PlayerActivity>(
                getString(R.string.video_id_key) to clickedVideoId
        ))
    }

    override fun onListItemLongClick(clickedVideoId: String) {
        activity!!.alert(getString(R.string.favourite_remove_confirmation)) {
            yesButton { mVideoDataViewModel.delete(VideoData(clickedVideoId)) }
            noButton { }
        }.show()
    }
}