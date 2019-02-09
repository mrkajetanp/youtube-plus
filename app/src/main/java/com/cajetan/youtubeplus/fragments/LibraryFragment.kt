package com.cajetan.youtubeplus.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cajetan.youtubeplus.R
import com.cajetan.youtubeplus.adapters.ContentListAdapter
import com.cajetan.youtubeplus.data.PlaylistData
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.MainDataViewModel
import com.cajetan.youtubeplus.utils.FeedItem
import com.cajetan.youtubeplus.utils.ItemType
import com.cajetan.youtubeplus.utils.YouTubeData
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class LibraryFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.PlaylistLibraryListener {

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mMainDataViewModel: MainDataViewModel

    private lateinit var videoList: RecyclerView
    private lateinit var progressBarCentre: ProgressBar
    private lateinit var noFavouritesView: TextView

    // TODO: caching results?

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setupDatabase()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter = ContentListAdapter(emptyList(), this, activity!!)
        mAdapter.onBottomReached = { }
        mYouTubeData = YouTubeData(activity!!, this)

        setupFavouritesList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        videoList = view.findViewById(R.id.videoList)
        progressBarCentre = view.findViewById(R.id.progressBarCentre)
        noFavouritesView = view.findViewById(R.id.noFavouritesView)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.others_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        else -> super.onOptionsItemSelected(item)
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
        mMainDataViewModel = ViewModelProviders.of(this).get(MainDataViewModel::class.java)

        mMainDataViewModel.getAllPlaylists().observe(this, Observer {
            if (it != null)
                loadPlaylists(it)
        })
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun loadPlaylists(playlistData: List<PlaylistData>) {
        videoList.visibility = View.INVISIBLE
        progressBarCentre.visibility = View.VISIBLE

        mYouTubeData.receivePlaylistsLibraryResults(playlistData)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onPlaylistsReceived(results: List<FeedItem>) {
        mAdapter.clearItems()
        mAdapter.addItems(results)

        noFavouritesView.visibility = if (mAdapter.itemCount == 0) View.VISIBLE else View.GONE
        progressBarCentre.visibility = View.INVISIBLE
        videoList.visibility = View.VISIBLE
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_library_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))

            ItemType.Playlist -> findNavController().navigate(R.id.action_library_to_playerActivity,
                    bundleOf(getString(R.string.playlist_id_key) to id))
        }
    }

    override fun onListItemLongClick(id: String, type: ItemType) {
        when (type) {
            ItemType.Video ->
                activity!!.alert(getString(R.string.favourite_remove_confirmation)) {
                    yesButton { mMainDataViewModel.deleteFavourite(VideoData(id)) }
                    noButton { }
                }.show()

            ItemType.Playlist ->
                activity!!.alert(getString(R.string.playlist_remove_confirmation)) {
                    yesButton { mMainDataViewModel.deletePlaylist(PlaylistData(id)) }
                    noButton { }
                }.show()
        }
    }
}