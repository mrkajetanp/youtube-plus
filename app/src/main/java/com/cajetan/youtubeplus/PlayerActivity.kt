package com.cajetan.youtubeplus

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import androidx.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.DialogFragment
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.cajetan.youtubeplus.data.VideoData
import com.cajetan.youtubeplus.data.VideoDataViewModel
import com.cajetan.youtubeplus.utils.FullScreenHelper
import com.cajetan.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.Video
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.menu.MenuItem
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.menu.YouTubePlayerMenu
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker
import kotlinx.android.synthetic.main.activity_player.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.net.URL

class PlayerActivity : AppCompatActivity(), YouTubeData.VideoDataListener,
        SeekDialog.SeekDialogListener, YouTubeData.PlaylistDataListener {

    private val TAG: String = this.javaClass.simpleName

    private val mFullScreenHelper: FullScreenHelper = FullScreenHelper(this)

    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder

    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mUIController: PlayerUIController

    private lateinit var mVideoId: String
    private lateinit var mVideoData: Video
    private lateinit var mVideoThumbnail: Bitmap

    private val mContext: Context = this
    private lateinit var mVideoDataViewModel: VideoDataViewModel

    private val mTracker: YouTubePlayerTracker = YouTubePlayerTracker()

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.extras?.getString(getString(R.string.player_action_key)) ==
                    getString(R.string.player_action_play_pause)) {
                mainPlayerView?.togglePlayPause()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mYouTubeData = YouTubeData(this)

        val playlistId = getPlaylistId(intent)
        if (playlistId == null) {
            mVideoId = getIntentVideoId(intent!!)
        } else {
            mVideoId = playlistId
            mYouTubeData.receivePlaylistResults(playlistId)
        }

        mYouTubeData.receiveVideoData(mVideoId)
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel::class.java)

        setupPlayer(mVideoId)
        setupMediaSession()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val playlistId = getPlaylistId(intent)
        if (playlistId == null) {
            mVideoId = getIntentVideoId(intent)
        } else {
            mVideoId = playlistId
            mYouTubeData.receivePlaylistResults(playlistId)
        }

        mainPlayerView.player.loadVideo(mVideoId, 0f)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mYouTubeData.receiveVideoData(mVideoId)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(0)
        unregisterReceiver(mBroadcastReceiver)
        mainPlayerView.release()
        super.onDestroy()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupPlayer(videoId: String) {
        registerReceiver(mBroadcastReceiver,
                IntentFilter().apply { addAction(Intent.ACTION_MEDIA_BUTTON) }
        )

        startService(Intent(this, PlayerLifecycleService::class.java))

        mainPlayerView.enableBackgroundPlayback(true)
        mainPlayerView.initialize({ initialisedYouTubePlayer ->
            initialisedYouTubePlayer.addListener(mTracker)
            initialisedYouTubePlayer.addListener(object: AbstractYouTubePlayerListener() {
                override fun onReady() {
                    initialisedYouTubePlayer.loadVideo(videoId, 0F)
                }

                override fun onStateChange(state: PlayerConstants.PlayerState) {
                    val playerState: Int? = when (state) {
                        PlayerConstants.PlayerState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
                        PlayerConstants.PlayerState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
                        PlayerConstants.PlayerState.ENDED -> PlaybackStateCompat.STATE_PAUSED
                        else -> null
                    }

                    if (playerState != null)
                        mStateBuilder.setState(playerState, mTracker.currentSecond.toLong(), 1f)

                    mMediaSession.setPlaybackState(mStateBuilder.build())
                    showMediaNotification(mStateBuilder.build())
                    super.onStateChange(state)
                }
            })
        }, true)

        mainPlayerView.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                mFullScreenHelper.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mFullScreenHelper.exitFullScreen()
            }
        })

        mUIController = mainPlayerView.playerUIController

        if (mUIController.menu == null)
            return

        mUIController.showMenuButton(true)
        val menu: YouTubePlayerMenu = mUIController.menu as YouTubePlayerMenu

        menu.addItem(MenuItem(getString(R.string.seek),
                R.drawable.ic_timer_black_24dp) {

            SeekDialog().apply {
                arguments = Bundle().apply {
                    putString(getString(R.string.duration_string_key),
                            mVideoData.contentDetails.duration)
                    putInt(getString(R.string.current_second_key),
                            Math.round(mTracker.currentSecond))
                }
            }.show(supportFragmentManager, getString(R.string.seeker_dialog_id))

            menu.dismiss()
        })

        addMenuItems(menu)
    }

    private fun addMenuItems(menu: YouTubePlayerMenu) {
        doAsync {
            val contains = mVideoDataViewModel.contains(mVideoId)

            val text = when (contains) {
                true -> "Remove from favourites"
                false -> getString(R.string.favourites_add)
            }

            val icon = when (contains) {
                true -> R.drawable.ic_star_black_24dp
                false -> R.drawable.ic_star_border_black_24dp
            }

            val message = when (contains) {
                true -> getString(R.string.favourites_removed)
                false -> getString(R.string.favourites_added)
            }

            uiThread {
                menu.addItem(MenuItem(text, icon) {
                    when (contains) {
                        true -> mVideoDataViewModel.delete(VideoData(mVideoId))
                        false -> mVideoDataViewModel.insert(VideoData(mVideoId))
                    }

                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    menu.dismiss()
                })
            }
        }
    }

    private fun setupMediaSession() {
        mMediaSession = MediaSessionCompat(this, TAG)

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mMediaSession.setMediaButtonReceiver(null)
        mStateBuilder = PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
//                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mMediaSession.setPlaybackState(mStateBuilder.build())
        mMediaSession.setCallback(PlayerSessionCallback())
        mMediaSession.isActive = true
    }

    private fun showMediaNotification(state: PlaybackStateCompat) {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING

        val icon = if (isPlaying) R.drawable.ic_pause_36dp else R.drawable.ic_play_36dp
        val playPause = if (isPlaying) getString(R.string.pause) else getString(R.string.play)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this,
                getString(R.string.notification_channel_id))

        val playPauseAction = NotificationCompat.Action(icon, playPause,
                PendingIntent.getBroadcast(this, 2923588,
                        Intent().apply {
                            action = Intent.ACTION_MEDIA_BUTTON
                            putExtra(getString(R.string.player_action_key),
                                    getString(R.string.player_action_play_pause))
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT))

        val contentPendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, PlayerActivity::class.java), 0)

        builder.setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_youtube_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(playPauseAction)
                .setOngoing(isPlaying)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mMediaSession.sessionToken))

        if (this::mVideoThumbnail.isInitialized) {
            builder.setContentTitle(mVideoData.snippet.title)
                    .setContentText(mVideoData.snippet.channelTitle)
                    .setLargeIcon(mVideoThumbnail)
        }

        if (!this::mVideoData.isInitialized) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(0)
            return
        }

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(0, builder.build())
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun getIntentVideoId(intent: Intent): String {
        val extras: Bundle = intent.extras!!
        val videoUrl: String = extras.getString(Intent.EXTRA_TEXT) ?: ""

        return when {
            videoUrl.isNotEmpty() -> videoUrl.substring(videoUrl.length - 11, videoUrl.length)

            extras.containsKey(getString(R.string.video_id_key)) ->
                extras.getString(getString(R.string.video_id_key)) as String

            else -> throw IllegalArgumentException("No video id available, cannot initialise the player")
        }
    }

    private fun getPlaylistId(intent: Intent?): String? {
        val videoUrl = intent?.extras?.getString(Intent.EXTRA_TEXT) ?: return null

        if (!videoUrl.contains("playlist?list="))
            return null

        return videoUrl.substring(videoUrl.indexOf("playlist?list=", 0) + 14)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks & others
    ////////////////////////////////////////////////////////////////////////////////

    private fun setAlbumArt(url: String) {
        doAsync {
            val bitmap = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())

            uiThread {
                mVideoThumbnail = bitmap
                showMediaNotification(mStateBuilder.build())
            }
        }
    }

    private inner class PlayerSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            mainPlayerView.togglePlayPause()
        }

        override fun onPause() {
            mainPlayerView.togglePlayPause()
        }
    }

    class PlayerLifecycleService : Service() {
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onTaskRemoved(rootIntent: Intent?) {
            super.onTaskRemoved(rootIntent)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(0)
            stopSelf()
        }
    }

    override fun onVideoDataReceived(videoData: Video) {
        mVideoData = videoData

        if (mVideoData.snippet.thumbnails.standard != null)
            setAlbumArt(mVideoData.snippet.thumbnails.standard.url)

        if (mVideoData.contentDetails.duration == getString(R.string.live_video_duration))
            mainPlayerView.playerUIController.enableLiveVideoUI(true)
    }

    override fun onPlaylistDataReceived(results: List<PlaylistItem>) {
        val builder = StringBuilder()

        for (item in results) {
            builder.append(item.snippet.title)
            builder.append('\n')
        }

        tempView.text = builder.toString()
    }

    override fun onSeekButtonClicked(duration: Float) {
        mainPlayerView.player.seekTo(duration)
        mainPlayerView.resumePlayback()
    }
}
