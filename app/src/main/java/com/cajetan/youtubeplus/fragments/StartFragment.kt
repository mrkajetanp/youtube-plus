package com.cajetan.youtubeplus.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cajetan.youtubeplus.R
import com.cajetan.youtubeplus.adapters.ContentListAdapter
import com.cajetan.youtubeplus.data.PlaylistData
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.MainDataViewModel
import com.cajetan.youtubeplus.utils.FeedItem
import com.cajetan.youtubeplus.utils.ItemType
import com.cajetan.youtubeplus.utils.YouTubeData
import com.cajetan.youtubeplus.utils.hideKeyboard
import com.cajetan.youtubeplus.viewmodels.StartViewModel
import com.google.api.services.youtube.model.Video
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class StartFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.MostPopularListener {

    private val TAG: String = this.javaClass.simpleName

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData

    // TODO: view model in activity scope
    private lateinit var mMainDataViewModel: MainDataViewModel
    private lateinit var mStartViewModel: StartViewModel

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
        mStartViewModel = ViewModelProviders.of(this).get(StartViewModel::class.java)
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

        mAdapter = ContentListAdapter(mStartViewModel.getAdapterItems(), this, activity!!)
        mYouTubeData = YouTubeData(activity!!, this)

        setupVideoList()
        // TODO: default argument should be used here
        loadMostPopularVideos(mStartViewModel.nextPageToken)
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
        val searchView = menu.findItem(R.id.search)?.actionView as SearchView
        searchView.setQuery("", false)
        searchView.isIconified = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        R.id.action_where_do_we_go -> {
            findNavController().navigate(R.id.action_start_to_playerActivity,
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

    private fun setupVideoList() {
        mAdapter.onBottomReached = {
            loadMostPopularVideos(mStartViewModel.nextPageToken)
        }

        videoList.setHasFixedSize(true)
        videoList.adapter = mAdapter
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun loadMostPopularVideos(nextPageToken: String) {
        if (nextPageToken.isEmpty()) {
            progressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            progressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receiveMostPopularResults(nextPageToken)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onMostPopularReceived(results: List<Video>,
                                       nextPageToken: String, previousPageToken: String) {
        // TODO: possible reductions in logic here
        if (previousPageToken.isEmpty()) {
            mStartViewModel.clearAdapterItems()
            videoList.scrollToPosition(0)

            progressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            progressBarBottom.visibility = View.GONE
        }

        mStartViewModel.addAdapterItems(results.map { FeedItem(it.id, video = it) })
        mStartViewModel.nextPageToken = nextPageToken

        // TODO: observable LiveData
        mAdapter.setItems(mStartViewModel.getAdapterItems())
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_start_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))

            ItemType.Playlist -> findNavController().navigate(R.id.action_start_to_playerActivity,
                    bundleOf(getString(R.string.playlist_id_key) to id))

            else -> {}
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
