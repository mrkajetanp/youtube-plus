package com.cajetan.youtubeplus.utils

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.preference.Preference
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cajetan.youtubeplus.data.PlaylistData
import com.cajetan.youtubeplus.data.VideoData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoListResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.lang.UnsupportedOperationException
import java.util.Collections.emptyList
import java.util.concurrent.CancellationException

class YouTubeData(parentActivity: Activity, fragment: Fragment? = null) :
        EasyPermissions.PermissionCallbacks {

    companion object {
        private val TAG = "YouTubeData"

        private const val SEARCH_PAGE_SIZE = 20
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val REQUEST_AUTHORIZATION = 1001
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002

        private const val PREF_ACCOUNT_NAME = "accountName"
        private val SCOPES: Array<String> = arrayOf(YouTubeScopes.YOUTUBE_READONLY)
    }

    private val mActivity = parentActivity
    private val mFragment = fragment

    private val mCredential: GoogleAccountCredential = GoogleAccountCredential
            .usingOAuth2(parentActivity.applicationContext, SCOPES.toList())
            .setBackOff(ExponentialBackOff())

    private var accountInitialised: Boolean = false

    private var service: YouTube = YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(), mCredential)
                .setApplicationName("YouTube Plus")
                .build()

    private var mVideoId = ""
    private var mPlaylistId = ""
    private var searchQuery = ""
    private var pageToken = ""
    private var mChannelId = ""
    private lateinit var mVideoData: List<VideoData>
    private lateinit var mPlaylistsData: List<PlaylistData>
    private lateinit var mRequestType: RequestType

    private var favouritesCallback: ((List<Video>) -> List<Video>)? = null

    fun receiveVideoData(videoId: String) {
        mVideoId = videoId
        mRequestType = RequestType.DATA_REQUEST

        getResultsFromApi()
    }

    fun receiveSearchResults(search: String, pageToken: String) {
        searchQuery = search
        this.pageToken = pageToken
        mRequestType = RequestType.SEARCH_REQUEST

        getResultsFromApi()
    }

    fun receiveVideoListResults(videoData: List<VideoData>,
                                block: ((List<Video>) -> List<Video>)? = null) {
        mVideoData = videoData
        mRequestType = RequestType.VIDEO_LIST_REQUEST
        favouritesCallback = block

        getResultsFromApi()
    }

    fun receivePlaylistsLibraryResults(playlistsData: List<PlaylistData>) {
        mPlaylistsData = playlistsData
        mRequestType = RequestType.PLAYLISTS_LIBRARY_REQUEST

        getResultsFromApi()
    }

    fun receiveMostPopularResults(pageToken: String) {
        this.pageToken = pageToken
        mRequestType = RequestType.MOST_POPULAR_REQUEST

        getResultsFromApi()
    }

    fun receivePlaylistResults(playlistId: String, pageToken: String = "") {
        mPlaylistId = playlistId
        this.pageToken = pageToken
        mRequestType = RequestType.PLAYLIST_DATA_REQUEST

        getResultsFromApi()
    }

    fun receiveUploadPlaylistId(channelId: String) {
        mChannelId = channelId
        mRequestType = RequestType.UPLOAD_PLAYLIST_ID_REQUEST

        getResultsFromApi()
    }

    private fun getResultsFromApi() {
        when {
            !isGooglePlayServicesAvailable() -> acquireGooglePlayServices()
            mCredential.selectedAccountName == null && !accountInitialised -> chooseAccount()
            mCredential.selectedAccountName == null && accountInitialised -> { }
            !isDeviceOnline() -> Toast.makeText(mActivity, "The device is not online",
                    Toast.LENGTH_SHORT).show()
            else -> when (mRequestType) {
                RequestType.DATA_REQUEST -> videoDataTask(mVideoId)
                RequestType.SEARCH_REQUEST -> videoSearchTask(searchQuery, pageToken)
                RequestType.VIDEO_LIST_REQUEST -> videoListTask(mVideoData)
                RequestType.MOST_POPULAR_REQUEST -> mostPopularTask(pageToken)
                RequestType.PLAYLIST_DATA_REQUEST -> playlistDataTask(mPlaylistId, pageToken)
                RequestType.PLAYLISTS_LIBRARY_REQUEST -> playlistLibraryTask(mPlaylistsData)
                RequestType.UPLOAD_PLAYLIST_ID_REQUEST -> uploadPlaylistIdTask()
            }
        }
    }

    private fun videoSearchTask(videoId: String, pageToken: String) {
        doAsync {
            val result: ArrayList<FeedItem> = ArrayList()
            val nextPageToken: String
            val prevPageToken: String

            try {
                val searchList: YouTube.Search.List = service.search()
                        .list("id")
                        .setMaxResults(SEARCH_PAGE_SIZE.toLong())
                        .setQ(videoId)

                if (pageToken != "")
                    searchList.pageToken = pageToken

                val response = searchList.execute()

                nextPageToken = response.nextPageToken ?: ""
                prevPageToken = response.prevPageToken ?: ""

                // TODO: include channels and playlist in search results
                val finalVideoIds = StringBuilder()
                val finalPlaylistIds = StringBuilder()
                val finalChannelIds = StringBuilder()

                for (r in response.items) {
                    when (r.id.kind) {
                        "youtube#video" -> {
                            finalVideoIds.append(r.id.videoId)
                            finalVideoIds.append(',')
                        }
                        "youtube#playlist" -> {
                            finalPlaylistIds.append(r.id.playlistId)
                            finalPlaylistIds.append(',')
                        }
                        "youtube#channel" -> {
                            finalChannelIds.append(r.id.channelId)
                            finalChannelIds.append(',')
                        }
                    }
                }

                if (finalVideoIds.isNotEmpty())
                    finalVideoIds.setLength(finalVideoIds.length - 1)

                if (finalPlaylistIds.isNotEmpty())
                    finalPlaylistIds.setLength(finalPlaylistIds.length - 1)

                if (finalChannelIds.isNotEmpty())
                    finalChannelIds.setLength(finalChannelIds.length - 1)

                result.addAll(service.channels().list("snippet,statistics")
                        .setId(finalChannelIds.toString()).execute().items
                        .map { FeedItem(it.id, channel = it) })

                result.addAll(service.playlists().list("snippet,contentDetails")
                        .setId(finalPlaylistIds.toString()).execute().items
                        .map { FeedItem(it.id, playlist = it) })

                result.addAll(service.videos().list("snippet,contentDetails")
                        .setId(finalVideoIds.toString()).execute().items
                        .map { FeedItem(it.id, video = it) })

            } catch (e: java.lang.Exception) {
                onTaskCancelled(e)
                Log.d("YouTubeData", "Exception $e")
                throw CancellationException()
            }

            uiThread {
                val listener: VideoSearchListener? = when {
                    mActivity is VideoSearchListener -> mActivity
                    mFragment is VideoSearchListener -> mFragment
                    else -> null
                }

                listener?.onSearchResultsReceived(result.toList(), nextPageToken, prevPageToken)
                        ?: throw UnsupportedOperationException("Parent must implement VideoSearchListener")
            }
        }
    }

    // TODO: consider merging videoListTask with videoDataTask

    private fun videoDataTask(videoId: String) {
        doAsync {
            val result: Video
            try {
                result = service.videos().list("snippet,contentDetails")
                        .setId(videoId).execute().items[0]
            } catch (e: java.lang.Exception) {
                onTaskCancelled(e)
                throw CancellationException()
            }

            uiThread {
                val listener: VideoDataListener? = when {
                    mActivity is VideoDataListener -> mActivity
                    mFragment is VideoDataListener -> mFragment
                    else -> null
                }

                listener?.onVideoDataReceived(result)
                        ?: throw UnsupportedOperationException("Activity must implement VideoDataListener")
            }
        }
    }

    private fun videoListTask(videoData: List<VideoData>) {
        doAsync {
            val finalId = StringBuilder()
            for (data in videoData) {
                finalId.append(data.videoId)
                finalId.append(',')
            }

            val result: List<Video> = if (finalId.isEmpty()) {
                emptyList()
            } else {
                service.videos().list("snippet,contentDetails")
                        .setId(finalId.toString()).execute().items
            }

            uiThread {
                val listener: VideoListDataListener? = when {
                    mActivity is VideoListDataListener -> mActivity
                    mFragment is VideoListDataListener -> mFragment
                    else -> null
                }

                listener?.onVideoListReceived(result.toList(), favouritesCallback)
                        ?: throw UnsupportedOperationException("Activity must implement VideoListDataListener")
            }
        }
    }

    private fun playlistLibraryTask(playlists: List<PlaylistData>) {
        doAsync {
            val finalId = StringBuilder()
            for (playlist in playlists) {
                finalId.append(playlist.playlistId)
                finalId.append(',')
            }

            val result: List<FeedItem> = if (finalId.isEmpty()) {
                emptyList()
            } else {
                service.playlists().list("snippet,contentDetails")
                        .setId(finalId.toString()).execute().items
                        .map { FeedItem(it.id, playlist = it) }
            }

            uiThread {
                val listener: PlaylistLibraryListener? = when {
                    mActivity is PlaylistLibraryListener -> mActivity
                    mFragment is PlaylistLibraryListener -> mFragment
                    else -> null
                }

                listener?.onPlaylistsReceived(result.toList())
                        ?: throw UnsupportedOperationException("Activity must implement PlaylistLibraryListener")
            }
        }
    }

    private fun mostPopularTask(pageToken: String) {
        doAsync {
            val result: List<Video>
            val nextPageToken: String
            val prevPageToken: String

            Log.d("MainActivity", "Running the request")
            try {
                val searchList: YouTube.Videos.List = service.videos()
                        .list("snippet,contentDetails")
                        .setChart("mostPopular")
                        .setRegionCode("GB")

                if (pageToken != "")
                    searchList.pageToken = pageToken

                val response: VideoListResponse = searchList.execute()

                nextPageToken = response.nextPageToken ?: ""
                prevPageToken = response.prevPageToken ?: ""

                result = response.items
            } catch (e: java.lang.Exception) {
                onTaskCancelled(e)
                throw CancellationException()
            }
            Log.d("MainActivity", "Request complete")

            uiThread {
                val listener: MostPopularListener? = when {
                    mActivity is MostPopularListener-> mActivity
                    mFragment is MostPopularListener -> mFragment
                    else -> null
                }

                listener?.onMostPopularReceived(result, nextPageToken, prevPageToken)
                        ?: throw UnsupportedOperationException("Activity must implement MostPopularListener")
            }
        }
    }

    private fun playlistDataTask(playlistId: String, pageToken: String) {
        doAsync {
            val result: List<PlaylistItem>
            val nextPageToken: String
            val prevPageToken: String

            try {
                val list = service.playlistItems().list("snippet,contentDetails")
                        .setMaxResults(20).setPlaylistId(playlistId)


                if (pageToken.isNotEmpty())
                    list.pageToken = pageToken

                val response = list.execute()

                nextPageToken = response.nextPageToken ?: ""
                prevPageToken = response.prevPageToken ?: ""

                result = response.items
            } catch (e: java.lang.Exception) {
                onTaskCancelled(e)
                throw CancellationException()
            }

            uiThread {
                val listener: PlaylistDataListener? = when {
                    mActivity is PlaylistDataListener-> mActivity
                    mFragment is PlaylistDataListener -> mFragment
                    else -> null
                }

                listener?.onPlaylistDataReceived(result.toList(), nextPageToken, prevPageToken)
                        ?: throw UnsupportedOperationException("Activity must implement PlaylistDataListener")
            }
        }
    }

    // service.channels().list("").execute().items[0].contentDetails.relatedPlaylists.uploads

    private fun uploadPlaylistIdTask() {
        doAsync {
            val playlistId: String
            val channelTitle: String

            try {
                val responseItem = service.channels().list("snippet,contentDetails")
                        .setId(mChannelId).execute()
                        .items[0]

                playlistId = responseItem.contentDetails.relatedPlaylists.uploads
                channelTitle = responseItem.snippet.title
            } catch (e: java.lang.Exception) {
                onTaskCancelled(e)
                throw CancellationException()
            }

            uiThread {
                val listener: UploadPlaylistListener? = when {
                    mActivity is UploadPlaylistListener -> mActivity
                    mFragment is UploadPlaylistListener -> mFragment
                    else -> null
                }

                listener?.onUploadPlaylistIdReceived(playlistId, channelTitle)
                        ?: throw UnsupportedOperationException("Activity must implement UploadPlaylistListener")
            }
        }
    }

    private fun onTaskCancelled(error: Exception) {
        when (error) {
            is GooglePlayServicesAvailabilityIOException ->
                Toast.makeText(mActivity, "GooglePlayServices not available", Toast.LENGTH_LONG).show()
            is UserRecoverableAuthIOException ->
                mActivity.startActivityForResult(error.intent, YouTubeData.REQUEST_AUTHORIZATION)
            else -> Log.e(TAG, "Error occurred ${error.message}")
        }
    }

    fun onParentActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                if (resultCode != RESULT_OK) {
                    Toast.makeText(mActivity,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",
                            Toast.LENGTH_LONG).show()
                } else {
                    getResultsFromApi()
                }
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == RESULT_OK && data?.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        val settings = PreferenceManager.getDefaultSharedPreferences(mActivity)

                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
                        editor.apply()

                        mCredential.selectedAccountName = accountName
                        getResultsFromApi()
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (resultCode == RESULT_OK)
                    getResultsFromApi()
            }
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(mActivity, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = PreferenceManager.getDefaultSharedPreferences(mActivity)
                    .getString(PREF_ACCOUNT_NAME, null)

            if (accountName != null) {
                mCredential.selectedAccountName = accountName
                accountInitialised = true
                getResultsFromApi()
            } else {
                mActivity.startActivityForResult(mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            EasyPermissions.requestPermissions(mActivity,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val conMgr = mActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conMgr.activeNetworkInfo
        return networkInfo?.isConnected ?: false
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        return apiAvailability.isGooglePlayServicesAvailable(mActivity) == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity)

        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            Toast.makeText(mActivity, "GooglePlayServices not available", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        getResultsFromApi()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults, this)
    }

    private enum class RequestType {
        DATA_REQUEST,
        SEARCH_REQUEST,
        VIDEO_LIST_REQUEST,
        MOST_POPULAR_REQUEST,
        PLAYLIST_DATA_REQUEST,
        PLAYLISTS_LIBRARY_REQUEST,
        UPLOAD_PLAYLIST_ID_REQUEST
    }

    interface VideoDataListener {
        fun onVideoDataReceived(videoData: Video)
    }

    interface VideoSearchListener {
        fun onSearchResultsReceived(results: List<FeedItem>,
                                    nextPageToken: String, previousPageToken: String)
    }

    interface VideoListDataListener {
        fun onVideoListReceived(results: List<Video>,
                                block: ((List<Video>) -> List<Video>)? = null)
    }

    interface PlaylistLibraryListener {
        fun onPlaylistsReceived(results: List<FeedItem>)
    }

    interface PlaylistDataListener {
        fun onPlaylistDataReceived(results: List<PlaylistItem>,
                                   nextPageToken: String, previousPageToken: String)
    }

    interface MostPopularListener {
        fun onMostPopularReceived(results: List<Video>,
                                  nextPageToken: String, previousPageToken: String)
    }

    interface UploadPlaylistListener {
        fun onUploadPlaylistIdReceived(id: String, channelTitle: String)
    }
}
