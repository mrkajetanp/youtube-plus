package com.cajetanp.youtubeplus.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
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
import com.cajetanp.youtubeplus.utils.hideKeyboard
import com.cajetanp.youtubeplus.viewmodels.FavouritesViewModel
import com.google.api.services.youtube.model.Video
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class FavouritesFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.VideoListDataListener {

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private var mMenu: Menu? = null

    private lateinit var mFavouritesViewModel: FavouritesViewModel
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
        mFavouritesViewModel = ViewModelProviders.of(this).get(FavouritesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        videoList = view.findViewById(R.id.videoList)
        progressBarCentre = view.findViewById(R.id.progressBarCentre)
        noFavouritesView = view.findViewById(R.id.noFavouritesView)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter = ContentListAdapter(mFavouritesViewModel.getAdapterItems(), this, activity!!)
        mAdapter.onBottomReached = { }
        mYouTubeData = YouTubeData(activity!!, this)

        setupFavouritesList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_options_menu, menu)

        mMenu = menu

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        searchView.queryHint = getString(R.string.search_favourites)
        searchView.maxWidth = Integer.MAX_VALUE

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.adjustSearchView(mFavouritesViewModel.filterQuery)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        R.id.action_where_do_we_go -> {
            findNavController().navigate(R.id.action_favourites_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to getString(R.string.where_do_we_go_id)))
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        activity?.hideKeyboard()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupFavouritesList() {
        videoList.setHasFixedSize(true)
        videoList.adapter = mAdapter
    }

    private fun setupDatabase() {
        mMainDataViewModel = ViewModelProviders.of(this).get(MainDataViewModel::class.java)

        mMainDataViewModel.getAllFavourites().observe(this, Observer {
            if (it != null) {
                mMenu?.adjustSearchView(mFavouritesViewModel.filterQuery)

                if (mFavouritesViewModel.filterQuery.isEmpty())
                    loadFavourites(it)
                else
                    filterVideos(mFavouritesViewModel.filterQuery)
            }
        })
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    fun filterVideos(query: String) {
        if (query.isEmpty())
            return

        mFavouritesViewModel.filterQuery = query

        loadFavourites(mMainDataViewModel.getAllFavourites().value!!) {
            it.filter { t -> t.snippet.title.toLowerCase().contains(query.toLowerCase()) }
        }
    }

    /**
     * A helper method to iconify/expand SearchView and adjust its content based on the filterQuery
     * If the query is empty, it'll clear the content and iconify the SearchView
     * If the query is not empty, it'll make sure the SearchView is expanded and reflects it
     * Must be called on a Menu object reflecting the current OptionsMenu
     * Won't do anything if the current OptionsMenu doesn't contain a SearchView with id "search"
     * */
    private fun Menu.adjustSearchView(filterQuery: String) {
        val searchView = this.findItem(R.id.search)?.actionView as SearchView?

        if (filterQuery.isEmpty()) {
            searchView?.setQuery("", false)
            searchView?.isIconified = true
        } else {
            searchView?.setQuery(filterQuery, false)
            searchView?.isIconified = false
        }

        searchView?.clearFocus()
    }

    private fun loadFavourites(videoData: List<VideoData>,
                               block: ((List<Video>) -> List<Video>)? = null) {
        videoList.visibility = View.INVISIBLE
        progressBarCentre.visibility = View.VISIBLE

        mYouTubeData.receiveVideoListResults(videoData, block)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onVideoListReceived(results: List<Video>,
                                     block: ((List<Video>) -> List<Video>)?) {
        mFavouritesViewModel.clearAdapterItems()

        // If an additional function was passed, apply it to the results
        val result = block?.invoke(results)?.toList() ?: results.toList()

        mFavouritesViewModel.addAdapterItems(result.map { FeedItem(it.id, video = it) })

        noFavouritesView.visibility = if (mFavouritesViewModel.getAdapterItems().size == 0) View.VISIBLE else View.GONE
        progressBarCentre.visibility = View.INVISIBLE
        videoList.visibility = View.VISIBLE

        mAdapter.setItems(mFavouritesViewModel.getAdapterItems())
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_favourites_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))

            ItemType.Playlist -> findNavController().navigate(R.id.action_favourites_to_playerActivity,
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