package com.cajetan.youtubeplus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TEST_VIDEO_ID = "Bcqb7kzekoc";

    // TODO: implement auto fullscreen on rotation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        Intent testVideoPlayerIntent = new Intent(this, PlayerActivity.class);
        testVideoPlayerIntent.putExtra("video_id", TEST_VIDEO_ID);
        startActivity(testVideoPlayerIntent);
    }

    private void createNotificationChannel() {
        // No need for Notification Channels prior to Oreo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel("main-channel-id",
                getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
