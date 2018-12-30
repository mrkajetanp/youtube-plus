package com.cajetan.youtubeplus

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.VideoDataViewModel
import com.cajetan.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.Video

class FavouritesActivity : AppCompatActivity(),
        YouTubeData.FavouritesDataListener, FavouriteListAdapter.ListItemClickListener  {

    private var mVideoDataViewModel: VideoDataViewModel? = null
    private var mNoFavouritesView: TextView? = null

    private var mFavouriteList: RecyclerView? = null
    private var mAdapter: FavouriteListAdapter = FavouriteListAdapter(emptyList(), this, this)

    private var mBottomNavBar: BottomNavigationView? = null
    private var mProgressBarCentre: ProgressBar? = null

    private var mYouTubeData: YouTubeData? = null

    // TODO: caching results?

    ///////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        mYouTubeData = YouTubeData(this)
        mProgressBarCentre = findViewById(R.id.progress_bar_centre)
        mNoFavouritesView = findViewById(R.id.no_favourites_view)

        setupFavouritesList()
        setupDatabase()
        setupBottomBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData?.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Init
    ///////////////////////////////////////////////////////////////////////////////

    private fun setupFavouritesList() {
        mFavouriteList = findViewById(R.id.favourite_list)
        mFavouriteList?.layoutManager = LinearLayoutManager(this)
        mFavouriteList?.setHasFixedSize(false)
        mFavouriteList?.adapter = mAdapter
    }

    private fun setupDatabase() {
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
        mVideoDataViewModel?.allVideoData?.observe(this, Observer {
            if (it != null)
                loadFavourites(it)
        })
    }

    private fun setupBottomBar() {
        mBottomNavBar = findViewById(R.id.bottom_bar)
        mBottomNavBar?.selectedItemId = R.id.action_favourites
        mBottomNavBar?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_start -> {
                    finish()
                    it.setChecked(true)
                    true
                }

                R.id.action_favourites -> {
                    it.setChecked(true)
                    true
                }

                R.id.action_others -> {
//                    it.setChecked(true)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////////

    private fun loadFavourites(videoData: List<VideoData>) {
        mFavouriteList?.visibility = View.INVISIBLE
        mProgressBarCentre?.visibility = View.VISIBLE

        mYouTubeData?.receiveFavouritesResults(videoData)
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Callbacks and others
    ///////////////////////////////////////////////////////////////////////////////

    override fun onFavouritesReceived(results: MutableList<Video>?) {
        mAdapter.clearItems()
        mAdapter.addItems(results)

        if (mAdapter.itemCount == 0)
            mNoFavouritesView?.visibility = View.VISIBLE
        else
            mNoFavouritesView?.visibility = View.GONE

        mProgressBarCentre?.visibility = View.INVISIBLE
        mFavouriteList?.visibility = View.VISIBLE
    }

    override fun onListItemClick(clickedVideoId: String?) {
        val videoPlayerIntent = Intent(this, PlayerActivity::class.java)
        videoPlayerIntent.putExtra(getString(R.string.video_id_key), clickedVideoId)
        startActivity(videoPlayerIntent)
    }

    override fun onListItemLongClick(clickedVideoId: String?) {
        val videoId: String = clickedVideoId as String

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.favourite_remove_confirmation))
                .setPositiveButton(android.R.string.yes) {
                    _, _ -> mVideoDataViewModel?.delete(VideoData(videoId))
                }.setNegativeButton(android.R.string.no, null).show()
    }
}

