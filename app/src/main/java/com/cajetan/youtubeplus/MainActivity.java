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
}
