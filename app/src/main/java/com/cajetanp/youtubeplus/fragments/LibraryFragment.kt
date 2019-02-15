package com.cajetanp.youtubeplus.fragments

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
import com.cajetanp.youtubeplus.R
import com.cajetanp.youtubeplus.adapters.ContentListAdapter
import com.cajetanp.youtubeplus.data.PlaylistData
import com.cajetanp.youtubeplus.data.VideoData
import com.cajetanp.youtubeplus.data.MainDataViewModel
import com.cajetanp.youtubeplus.utils.FeedItem
import com.cajetanp.youtubeplus.utils.ItemType
import com.cajetanp.youtubeplus.utils.YouTubeData
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class LibraryFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.PlaylistLibraryListener {

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mMainDataViewModel: MainDataViewModel

    private lateinit var contentList: RecyclerView
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

        setupPlaylistsList()

        if (mMainDataViewModel.getAllPlaylists().value != null)
            loadPlaylists(mMainDataViewModel.getAllPlaylists().value!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        contentList = view.findViewById(R.id.contentList)
        progressBarCentre = view.findViewById(R.id.progressBarCentre)
        noFavouritesView = view.findViewById(R.id.noPlaylistsView)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.basic_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        R.id.action_where_do_we_go -> {
            findNavController().navigate(R.id.action_library_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to getString(R.string.where_do_we_go_id)))
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

    private fun setupPlaylistsList() {
        contentList.layoutManager = LinearLayoutManager(activity!!)
        contentList.setHasFixedSize(false)
        contentList.adapter = mAdapter
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
        contentList.visibility = View.INVISIBLE
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
        contentList.visibility = View.VISIBLE
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_library_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))

            ItemType.Playlist -> findNavController().navigate(R.id.action_library_to_playerActivity,
                    bundleOf(getString(R.string.playlist_id_key) to id))

            else -> {}
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

            else -> {}
        }
    }
}