package com.cajetan.youtubeplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_favourites.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class MainActivity : AppCompatActivity(),
        YouTubeData.VideoSearchListener, YouTubeData.MostPopularListener,
        VideoListAdapter.ListItemClickListener {

    private val TAG = this.javaClass.simpleName

    private var mAdapter: VideoListAdapter = VideoListAdapter(emptyList(), this, this)
    private var mYouTubeData: YouTubeData? = null
    private var mContext: Context? = null
    private var mVideoDataViewModel: VideoDataViewModel? = null

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
        mContext = this

        setupSearchResultList()
        createNotificationChannel()
        setupBottomBar()
        handleIntent(intent)
        loadMostPopularVideos(null)

        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        // TODO: see if the null check is unnecessary
        if (bottom_bar_main != null)
            bottom_bar_main.selectedItemId = R.id.action_start
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
        mYouTubeData?.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupSearchResultList() {
        search_results.layoutManager = LinearLayoutManager(this)

        mAdapter.setOnBottomReachedListener {
            Log.d(TAG, "Reached the bottom")

            if (mSearchQuery == "")
                loadMostPopularVideos(mNextPageToken)
            else
                videoSearch(mNextPageToken)
        }

        search_results.setHasFixedSize(false)
        search_results.adapter = mAdapter
    }

    private fun setupBottomBar() {
        bottom_bar_main.selectedItemId = R.id.action_start
        bottom_bar_main.setOnNavigationItemSelectedListener {
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
            search_progress_bar_centre.visibility = View.VISIBLE
            search_results.visibility = View.INVISIBLE
        } else {
            search_progress_bar_bottom.visibility = View.VISIBLE
        }

        Log.d(TAG, "Searching for a video $mSearchQuery")
        mYouTubeData?.receiveSearchResults(mSearchQuery, nextPageToken)
    }

    private fun loadMostPopularVideos(nextPageToken: String?) {
        if (nextPageToken == null) {
            search_progress_bar_centre.visibility = View.VISIBLE
            search_results.visibility = View.INVISIBLE
        } else {
            search_progress_bar_bottom.visibility = View.VISIBLE
        }

        mYouTubeData?.receiveMostPopularResults(nextPageToken)
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
            search_results.scrollToPosition(0)

            search_progress_bar_centre.visibility = View.INVISIBLE
            search_results.visibility = View.VISIBLE
        } else {
            search_progress_bar_bottom.visibility = View.GONE
        }

        mAdapter.addItems(results)
        mNextPageToken = nextPageToken as String
    }

    override fun onMostPopularReceived(results: MutableList<Video>?,
                                       nextPageToken: String?, previousPageToken: String?) {
        if (previousPageToken == null || previousPageToken == "") {
            mAdapter.clearItems()
            search_results.scrollToPosition(0)

            search_progress_bar_centre.visibility = View.INVISIBLE
            search_results.visibility = View.VISIBLE
        } else {
            search_progress_bar_bottom.visibility = View.GONE
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
            yesButton { mVideoDataViewModel?.insert(VideoData(clickedVideoId as String)) }
            noButton { }
        }.show()
    }
}
