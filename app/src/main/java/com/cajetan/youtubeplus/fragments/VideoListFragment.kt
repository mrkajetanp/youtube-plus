package com.cajetan.youtubeplus.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class VideoListFragment : Fragment(), VideoListAdapter.ListItemClickListener,
        YouTubeData.MostPopularListener, YouTubeData.VideoSearchListener {

    private val TAG: String = this.javaClass.simpleName

    private lateinit var mAdapter: VideoListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mVideoDataViewModel: VideoDataViewModel

    private lateinit var videoList: RecyclerView
    private lateinit var searchProgressBarCentre: ProgressBar
    private lateinit var searchProgressBarBottom: ProgressBar

    private var mSearchQuery: String = ""
    private var mNextPageToken: String = ""
    private var searching = false

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter  = VideoListAdapter(emptyList(), this, activity!!)
        mYouTubeData = YouTubeData(activity!!, this)

        setupSearchResultList()
        loadMostPopularVideos("")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.video_list_fragment, container, false)

        videoList = view.findViewById(R.id.videoList)
        searchProgressBarCentre = view.findViewById(R.id.searchProgressBarCentre)
        searchProgressBarBottom = view.findViewById(R.id.searchProgressBarBottom)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupSearchResultList() {
        videoList.layoutManager = LinearLayoutManager(activity!!)

        mAdapter.onBottomReached = {
            if (mSearchQuery.isEmpty())
                loadMostPopularVideos(mNextPageToken)
            else
                loadSearchResults(mNextPageToken)
        }

        videoList.setHasFixedSize(false)
        videoList.adapter = mAdapter
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    fun searchVideos(query: String) {
        if (query.isEmpty() || query == mSearchQuery)
            return

        mSearchQuery = query
        loadSearchResults("")
    }

    private fun loadSearchResults(nextPageToken: String) {
        if (!searching) {
            mNextPageToken = ""
            searching = true
        }

        if (nextPageToken.isEmpty()) {
            searchProgressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            searchProgressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receiveSearchResults(mSearchQuery, nextPageToken)
    }

    private fun loadMostPopularVideos(nextPageToken: String) {
        if (nextPageToken.isEmpty()) {
            searchProgressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            searchProgressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receiveMostPopularResults(nextPageToken)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onSearchResultsReceived(results: List<Video>,
                                         nextPageToken: String, previousPageToken: String) {
        if (previousPageToken.isEmpty()) {
            mAdapter.clearItems()
            videoList.scrollToPosition(0)

            searchProgressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            searchProgressBarBottom.visibility = View.GONE
        }

        mAdapter.addItems(results)
        mNextPageToken = nextPageToken
    }

    override fun onMostPopularReceived(results: List<Video>,
                                       nextPageToken: String, previousPageToken: String) {
        if (previousPageToken.isEmpty()) {
            mAdapter.clearItems()
            videoList.scrollToPosition(0)

            searchProgressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            searchProgressBarBottom.visibility = View.GONE
        }

        mAdapter.addItems(results)
        mNextPageToken = nextPageToken
    }

    override fun onListItemClick(clickedVideoId: String) {
        startActivity(activity!!.intentFor<PlayerActivity>(
                getString(R.string.video_id_key) to clickedVideoId
        ))
    }

    override fun onListItemLongClick(clickedVideoId: String) {
        activity!!.alert(getString(R.string.favourite_add_confirmation)) {
            // TODO: possibly delegate to the activity
            yesButton { mVideoDataViewModel.insert(VideoData(clickedVideoId)) }
            noButton { }
        }.show()
    }
}
