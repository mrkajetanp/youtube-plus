package com.cajetan.youtubeplus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cajetan.youtubeplus.utils.YouTubeData;

public class MainActivity extends AppCompatActivity implements YouTubeData.VideoSearchListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TEST_VIDEO_ID = "Bcqb7kzekoc";

    private EditText searchBox;
    private TextView searchResultView;
    private ProgressBar searchProgressBar;

    // TODO: implement auto fullscreen on rotation
    private YouTubeData mYouTubeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mYouTubeData = new YouTubeData(this);

        searchBox = findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    videoSearch(textView.getText().toString());
                    handled = true;
                }

                return handled;
            }
        });

        searchResultView = findViewById(R.id.search_results);
        searchProgressBar = findViewById(R.id.search_progress_bar);

        createNotificationChannel();
    }

    public void playTestVideo(View view) {
        Intent testVideoPlayerIntent = new Intent(this, PlayerActivity.class);
        testVideoPlayerIntent.putExtra(getString(R.string.video_id_key), TEST_VIDEO_ID);
        startActivity(testVideoPlayerIntent);
    }

    public void videoSearch(String query) {
        searchResultView.setVisibility(View.INVISIBLE);
        searchProgressBar.setVisibility(View.VISIBLE);

        Log.d("YouTubeData", "Searching for a video: " + searchBox.getText().toString());
        mYouTubeData.receiveSearchResults(query);
    }

    private void createNotificationChannel() {
        // No need for Notification Channels prior to Oreo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public void onSearchResultsReceived(String results) {
        searchResultView.setText(results);

        searchProgressBar.setVisibility(View.INVISIBLE);
        searchResultView.setVisibility(View.VISIBLE);
    }

    // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
