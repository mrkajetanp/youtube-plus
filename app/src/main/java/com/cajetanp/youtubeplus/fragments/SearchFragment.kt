package com.cajetanp.youtubeplus.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cajetanp.youtubeplus.R
import com.cajetanp.youtubeplus.adapters.ContentListAdapter
import com.cajetanp.youtubeplus.data.MainDataViewModel
import com.cajetanp.youtubeplus.data.PlaylistData
import com.cajetanp.youtubeplus.data.VideoData
import com.cajetanp.youtubeplus.utils.FeedItem
import com.cajetanp.youtubeplus.utils.ItemType
import com.cajetanp.youtubeplus.utils.YouTubeData
import com.cajetanp.youtubeplus.utils.adjustSearchView
import com.cajetanp.youtubeplus.viewmodels.SearchViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class SearchFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.VideoSearchListener, YouTubeData.UploadPlaylistListener {

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData

    private lateinit var mMainDataViewModel: MainDataViewModel
    private lateinit var mSearchViewModel: SearchViewModel

    private lateinit var rootView: View
    private lateinit var videoList: RecyclerView
    private lateinit var progressBarCentre: ProgressBar
    private lateinit var progressBarBottom: ProgressBar

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        mMainDataViewModel = ViewModelProviders.of(this).get(MainDataViewModel::class.java)
        mSearchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        rootView = view.findViewById(R.id.root_view)
        videoList = view.findViewById(R.id.videoList)
        progressBarCentre = view.findViewById(R.id.progressBarCentre)
        progressBarBottom = view.findViewById(R.id.progressBarBottom)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter = ContentListAdapter(mSearchViewModel.getAdapterItems(), this, activity!!)
        mYouTubeData = YouTubeData(activity!!, this)

        setupSearchResultList()

        val query = if (mSearchViewModel.searchQuery.isEmpty()) {
            arguments?.getString(getString(R.string.search_query_key))!!
        } else {
            mSearchViewModel.searchQuery
        }

        searchVideos(query)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_options_menu, menu)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.adjustSearchView(R.id.search, mSearchViewModel.searchQuery)
        rootView.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        R.id.action_where_do_we_go -> {
            findNavController().navigate(R.id.action_searchFragment_to_playerActivity,
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

    private fun setupSearchResultList() {
        mAdapter.onBottomReached = {
            loadSearchResults(mSearchViewModel.nextPageToken)
        }

        videoList.setHasFixedSize(true)
        videoList.adapter = mAdapter
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    fun searchVideos(query: String) {
        if (query.isEmpty() || query == mSearchViewModel.searchQuery)
            return

        mSearchViewModel.searchQuery = query
        loadSearchResults("")
    }

    private fun loadSearchResults(nextPageToken: String) {
        if (nextPageToken.isEmpty()) {
            progressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            progressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receiveSearchResults(mSearchViewModel.searchQuery, nextPageToken)
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onUploadPlaylistIdReceived(id: String, channelTitle: String) {
        findNavController().navigate(R.id.action_searchFragment_to_playlistContent,
                bundleOf(getString(R.string.playlist_id_key) to id,
                        getString(R.string.channel_title_key) to channelTitle))
    }


    override fun onSearchResultsReceived(results: List<FeedItem>,
                                         nextPageToken: String, previousPageToken: String) {
        if (previousPageToken.isEmpty()) {
            mSearchViewModel.clearAdapterItems()
            videoList.scrollToPosition(0)

            progressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            progressBarBottom.visibility = View.GONE
        }

        mSearchViewModel.addAdapterItems(results)
        mSearchViewModel.nextPageToken = nextPageToken

        // TODO: observable LiveData
        mAdapter.setItems(mSearchViewModel.getAdapterItems())
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_searchFragment_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))

            ItemType.Playlist -> findNavController().navigate(R.id.action_searchFragment_to_playerActivity,
                    bundleOf(getString(R.string.playlist_id_key) to id))

            ItemType.Channel -> mYouTubeData.receiveUploadPlaylistId(id)
        }
    }

    override fun onListItemLongClick(id: String, type: ItemType) {
        when (type) {
            ItemType.Video ->
                activity!!.alert(getString(R.string.favourite_add_confirmation)) {
                    yesButton { mMainDataViewModel.insertFavourite(VideoData(id)) }
                    noButton { }
                }.show()

            ItemType.Playlist ->
                activity!!.alert(getString(R.string.playlist_add_confirmation)) {
                    yesButton { mMainDataViewModel.insertPlaylist(PlaylistData(id)) }
                    noButton { }
                }.show()

            else -> {}
        }
    }
}