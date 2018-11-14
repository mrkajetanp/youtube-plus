package com.cajetan.youtubeplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cajetan.youtubeplus.utils.FullScreenHelper;
import com.cajetan.youtubeplus.utils.YouTubeData;
import com.google.api.services.youtube.model.Video;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker;

import java.io.IOException;
import java.net.URL;

public class PlayerActivity extends AppCompatActivity implements YouTubeData.VideoDataListener {
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private YouTubePlayerView mainPlayerView;
    private FullScreenHelper fullScreenHelper = new FullScreenHelper(this);

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private YouTubeData youTubeData;
    private String mVideoId;
    private Video mVideoData;
    private Bitmap mVideoThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mVideoId = getIntentVideoId();

        youTubeData = new YouTubeData(this);
        youTubeData.receiveVideoData(mVideoId);

        mainPlayerView = findViewById(R.id.main_player_view);

        startService(new Intent(this, PlayerLifecycleService.class));

        setupPlayer();
        setupMediaSession();
    }

    private void setupPlayer() {
        final String videoId = mVideoId;

        mainPlayerView.enableBackgroundPlayback(true);
        mainPlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(@NonNull final YouTubePlayer initialisedYouTubePlayer) {

                // Tracker to get the state of the player
                final YouTubePlayerTracker tracker = new YouTubePlayerTracker();
                initialisedYouTubePlayer.addListener(tracker);

                initialisedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        initialisedYouTubePlayer.loadVideo(videoId, 0);
                    }

                    @Override
                    public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
                        // TODO: not sure if it's a good idea to cast tbh

                        if (state == PlayerConstants.PlayerState.PLAYING) {
                            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    (long) tracker.getCurrentSecond(), 1f);
                        } else if (state == PlayerConstants.PlayerState.PAUSED) {
                            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                                    (long) tracker.getCurrentSecond(), 1f);
                        }

                        mMediaSession.setPlaybackState(mStateBuilder.build());
                        showMediaNotification(mStateBuilder.build());

                        super.onStateChange(state);
                    }
                });
            }
        }, true);

        mainPlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullScreenHelper.enterFullScreen();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullScreenHelper.exitFullScreen();
            }
        });
    }

    private void setupMediaSession() {
        mMediaSession = new MediaSessionCompat(this, TAG);

        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // For now, TODO: investigate
        mMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
//                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new PlayerSessionCallback());
        mMediaSession.setActive(true);
    }

    private void showMediaNotification(PlaybackStateCompat state) {
        // TODO: fix stuff
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

        int icon;
        String playPause;
        if (isPlaying) {
            icon = R.drawable.ic_pause_36dp;
            playPause = getString(R.string.pause);
        } else {
            icon = R.drawable.ic_play_36dp;
            playPause = getString(R.string.play);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.notification_channel_id));

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, playPause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, PlayerActivity.class), 0);

        // TODO: get title from YouTube API

        builder.setContentTitle("Video Title")
                .setContentText("Author")
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_youtube_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(playPauseAction)
                .setOngoing(isPlaying)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mMediaSession.getSessionToken()));

        // TODO: this will also have to be improved
        if (mVideoData != null) {
            builder.setContentTitle(mVideoData.getSnippet().getTitle())
                    .setContentText(mVideoData.getSnippet().getChannelTitle());

            if (mVideoThumbnail != null)
                builder.setLargeIcon(mVideoThumbnail);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private String getIntentVideoId() {
        String videoUrl = null;
        if (getIntent() != null && getIntent().getExtras() != null)
            videoUrl = getIntent().getExtras().getString(Intent.EXTRA_TEXT);

        String videoId;

        // Activity started by a regular Intent with a video id
        if (getIntent().getExtras().containsKey(getString(R.string.video_id_key)))
            videoId = getIntent().getExtras().getString(getString(R.string.video_id_key));
            // Activity started by a share Intent with a video url
        else if (videoUrl != null && !videoUrl.equals(""))
            videoId = videoUrl.substring(videoUrl.length() - 11, videoUrl.length());
            // No video to play, throw an exception
        else {
            // TODO: throw something more informative here
            throw new IllegalArgumentException("...");
        }

        return videoId;
    }

    private class SetAlbumArtTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap result = null;

            try {
                URL url = new URL(strings[0]);
                result = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mVideoThumbnail = bitmap;
            showMediaNotification(mStateBuilder.build());
        }
    }

    private class PlayerSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mainPlayerView.togglePlayPause();
        }

        @Override
        public void onPause() {
            mainPlayerView.togglePlayPause();
//            mainPlayerView.pausePlayback();
        }

//      @Override
//      public void onSkipToPrevious() {
//          super.onSkipToPrevious();
//      }
    }

    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    @Override
    public void onVideoDataReceived(Video videoData) {
        mVideoData = videoData;
        new SetAlbumArtTask().execute(mVideoData.getSnippet().getThumbnails().getStandard().getUrl());
        showMediaNotification(mStateBuilder.build());
    }

    // TODO: test with and without singletop

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        youTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // A service that calls necessary cleanup methods after the player is closed
    public static class PlayerLifecycleService extends Service {

        public PlayerLifecycleService() {
            super();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            super.onTaskRemoved(rootIntent);

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(0);

            stopSelf();
        }
    }

    @Override
    protected void onDestroy() {
        mainPlayerView.release();
        super.onDestroy();
    }
}
