package com.cajetan.youtubeplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.VideoDataViewModel
import com.cajetan.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.Video
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class MainActivity : AppCompatActivity(),
        YouTubeData.VideoSearchListener, YouTubeData.MostPopularListener,
        VideoListAdapter.ListItemClickListener {

    private val TAG = this.javaClass.simpleName

    private val mAdapter: VideoListAdapter = VideoListAdapter(emptyList(), this, this)
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mVideoDataViewModel: VideoDataViewModel

    private var mSearchQuery: String = ""
    private var mNextPageToken: String = ""
    private var searching = false

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mYouTubeData = YouTubeData(this)

        setupSearchResultList()
        createNotificationChannel()
        setupBottomBar()
        handleIntent(intent)
        loadMostPopularVideos(null)

        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        bottomBar.selectedItemId = R.id.action_start
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent as Intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupSearchResultList() {
        videoList.layoutManager = LinearLayoutManager(this)

        mAdapter.setOnBottomReachedListener {
            Log.d(TAG, "Reached the bottom")

            if (mSearchQuery == "")
                loadMostPopularVideos(mNextPageToken)
            else
                videoSearch(mNextPageToken)
        }

        videoList.setHasFixedSize(false)
        videoList.adapter = mAdapter
    }

    private fun setupBottomBar() {
        bottomBar.selectedItemId = R.id.action_start
        bottomBar.setOnNavigationItemSelectedListener {
             when (it.itemId) {
                R.id.action_start -> {
                    it.setChecked(true)
                    true
                }

                R.id.action_favourites -> {
                    startActivity(intentFor<FavouritesActivity>())
                    true
                }

                R.id.action_others -> {
                    it.setChecked(true)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        val channel = NotificationChannel(getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
        channel.description = getString(R.string.notification_channel_description)

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun videoSearch(nextPageToken: String?) {
        if (!searching) {
            mNextPageToken = ""
            searching = true
        }

        if (nextPageToken == null) {
            searchProgressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            searchProgressBarBottom.visibility = View.VISIBLE
        }

        Log.d(TAG, "Searching for a video $mSearchQuery")
        mYouTubeData.receiveSearchResults(mSearchQuery, nextPageToken)
    }

    private fun loadMostPopularVideos(nextPageToken: String?) {
        if (nextPageToken == null) {
            searchProgressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            searchProgressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receiveMostPopularResults(nextPageToken)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)

            if (query == "" || query == mSearchQuery)
                return

            mSearchQuery = query
            videoSearch(null)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks and others
    ////////////////////////////////////////////////////////////////////////////////

    override fun onSearchResultsReceived(results: MutableList<Video>?,
                                         nextPageToken: String?, previousPageToken: String?) {
        if (previousPageToken == null || previousPageToken == "") {
            mAdapter.clearItems()
            videoList.scrollToPosition(0)

            searchProgressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            searchProgressBarBottom.visibility = View.GONE
        }

        mAdapter.addItems(results)
        mNextPageToken = nextPageToken as String
    }

    override fun onMostPopularReceived(results: MutableList<Video>?,
                                       nextPageToken: String?, previousPageToken: String?) {
        if (previousPageToken == null || previousPageToken == "") {
            mAdapter.clearItems()
            videoList.scrollToPosition(0)

            searchProgressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            searchProgressBarBottom.visibility = View.GONE
        }

        mAdapter.addItems(results)
        mNextPageToken = nextPageToken as String
    }

    override fun onListItemClick(clickedVideoId: String?) {
        startActivity(intentFor<PlayerActivity>(
                getString(R.string.video_id_key) to clickedVideoId
        ))
    }

    override fun onListItemLongClick(clickedVideoId: String?) {
        alert(getString(R.string.favourite_add_confirmation)) {
            yesButton { mVideoDataViewModel.insert(VideoData(clickedVideoId as String)) }
            noButton { }
        }.show()
    }
}
