package com.cajetan.youtubeplus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cajetan.youtubeplus.utils.YouTubeData;
import com.google.api.services.youtube.model.SearchResult;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements YouTubeData.VideoSearchListener, VideoListAdapter.ListItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText searchBox;
    private VideoListAdapter mAdapter;
    private RecyclerView mVideoList;
    private ProgressBar searchProgressBar;

    // TODO: implement auto fullscreen on rotation
    private YouTubeData mYouTubeData;

    private String mSearchQuery = null;
    private String mNextPageToken = null;
    private String mPreviousPageToken = null;

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
                    mSearchQuery = textView.getText().toString();

                    videoSearch(mSearchQuery, null);
                    handled = true;
                }

                return handled;
            }
        });

        mVideoList = findViewById(R.id.search_results);
        searchProgressBar = findViewById(R.id.search_progress_bar);

        mVideoList.setLayoutManager(new LinearLayoutManager(this));

        // TODO investigate
        mVideoList.setHasFixedSize(true);

        createNotificationChannel();
    }

    // TODO: maybe lose the argument
    public void videoSearch(String query, String nextPageToken) {
        searchProgressBar.setVisibility(View.VISIBLE);
        mVideoList.setVisibility(View.INVISIBLE);

        Log.d("YouTubeData", "Searching for a video: " + searchBox.getText().toString());
        mYouTubeData.receiveSearchResults(query, nextPageToken);
    }

    public void onNextPageButton(View view) {
        videoSearch(mSearchQuery, mNextPageToken);
    }

    public void onPrevPageButton(View view) {
        videoSearch(mSearchQuery, mPreviousPageToken);
    }

    @Override
    public void onSearchResultsReceived(List<SearchResult> results,
                                        String nextPageToken, String previousPageToken) {

        mAdapter = new VideoListAdapter(results, this);
        mVideoList.setAdapter(mAdapter);

        searchProgressBar.setVisibility(View.INVISIBLE);
        mVideoList.setVisibility(View.VISIBLE);

        mNextPageToken = nextPageToken;
        mPreviousPageToken = previousPageToken;
    }

    @Override
    public void onListItemClick(String clickedVideoId) {
        Intent videoPlayerIntent = new Intent(this, PlayerActivity.class);
        videoPlayerIntent.putExtra(getString(R.string.video_id_key), clickedVideoId);
        startActivity(videoPlayerIntent);
    }

    // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
}
