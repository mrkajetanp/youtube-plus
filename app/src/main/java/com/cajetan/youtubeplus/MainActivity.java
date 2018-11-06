package com.cajetan.youtubeplus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cajetan.youtubeplus.utils.FullScreenHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TEST_VIDEO_ID = "Bcqb7kzekoc";

    private YouTubePlayerView mainPlayerView;
    private FullScreenHelper fullScreenHelper = new FullScreenHelper(this);

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private NotificationManager mNotificationManager;

    // TODO: implement auto fullscreen on rotation

    // TODO: player to a separate activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainPlayerView = findViewById(R.id.main_player_view);

        setupPlayer();
        createNotificationChannel();
        setupMediaSession();
    }

    private void setupPlayer() {
        // If the activity was started by an intent, get the video link
        // Extract the id and set the shared video to play
        // Otherwise for the time being just play the testing video
        String videoUrl = null;
        if (getIntent() != null && getIntent().getExtras() != null)
            videoUrl = getIntent().getExtras().getString(Intent.EXTRA_TEXT);

        final String videoId;
        if (videoUrl != null && !videoUrl.equals(""))
            videoId = videoUrl.substring(videoUrl.length()-11, videoUrl.length());
        else
            videoId = TEST_VIDEO_ID;

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
            playPause = "Pause";
        } else {
            icon = R.drawable.ic_play_36dp;
            playPause = "Play";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"main-channel-id");

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, playPause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), 0);

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


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        // TODO: extract
        CharSequence name = "YouTube Plus Notification Channel";
        String description = "Main channel for the notification";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel channel = new NotificationChannel("main-channel-id", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
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

//        @Override
//        public void onSkipToPrevious() {
//            super.onSkipToPrevious();
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO: implement now with single top
        super.onNewIntent(intent);
    }

    public void onTestButtonClicked(View v) {
        mainPlayerView.togglePlayPause();
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
    protected void onDestroy() {
        mainPlayerView.release();
        super.onDestroy();
    }
}
