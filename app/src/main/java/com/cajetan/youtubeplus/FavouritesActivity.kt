package com.cajetan.youtubeplus

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.VideoDataViewModel
import com.cajetan.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.Video
import kotlinx.android.synthetic.main.activity_favourites.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class FavouritesActivity : AppCompatActivity(),
        YouTubeData.FavouritesDataListener, FavouriteListAdapter.ListItemClickListener  {

    // TODO: convert ids to camelCase

    private var mVideoDataViewModel: VideoDataViewModel? = null
    private var mAdapter: FavouriteListAdapter = FavouriteListAdapter(emptyList(), this, this)

    private var mYouTubeData: YouTubeData? = null

    // TODO: caching results?

    ///////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        mYouTubeData = YouTubeData(this)

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
        favourite_list.layoutManager = LinearLayoutManager(this)
        favourite_list.setHasFixedSize(false)
        favourite_list.adapter = mAdapter
    }

    private fun setupDatabase() {
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)
        mVideoDataViewModel?.getAllVideoData()?.observe(this, Observer {
            if (it != null)
                loadFavourites(it)
        })
    }

    private fun setupBottomBar() {
        bottom_bar.selectedItemId = R.id.action_favourites
        bottom_bar.setOnNavigationItemSelectedListener {
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
        favourite_list.visibility = View.INVISIBLE
        progress_bar_centre.visibility = View.VISIBLE

        mYouTubeData?.receiveFavouritesResults(videoData)
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Callbacks and others
    ///////////////////////////////////////////////////////////////////////////////

    override fun onFavouritesReceived(results: MutableList<Video>?) {
        mAdapter.clearItems()
        mAdapter.addItems(results)

        if (mAdapter.itemCount == 0)
            no_favourites_view.visibility = View.VISIBLE
        else
            no_favourites_view.visibility = View.GONE

        progress_bar_centre.visibility = View.INVISIBLE
        favourite_list.visibility = View.VISIBLE
    }

    override fun onListItemClick(clickedVideoId: String?) {
        startActivity(intentFor<PlayerActivity>(
                getString(R.string.video_id_key) to clickedVideoId
        ))
    }

    override fun onListItemLongClick(clickedVideoId: String?) {
        alert(getString(R.string.favourite_remove_confirmation)) {
            yesButton { mVideoDataViewModel?.delete(VideoData(clickedVideoId as String)) }
            noButton { }
        }.show()
    }
}

