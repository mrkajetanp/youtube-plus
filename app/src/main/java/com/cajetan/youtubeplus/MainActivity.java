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
import android.widget.Button;
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

    private YouTubeData mYouTubeData;

    private String mSearchQuery = null;

    // TODO: implement auto fullscreen on rotation

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
                    String textViewContent = textView.getText().toString();

                    if (textViewContent.equals("") || textViewContent.equals(mSearchQuery))
                        return true;

                    mSearchQuery = textViewContent;

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

    @Override
    public void onSearchResultsReceived(List<SearchResult> results,
                                        final String nextPageToken, String previousPageToken) {

        List<SearchResult> finalResults = null;
        if (mAdapter != null)
            finalResults = mAdapter.getSearchResults();

        // Reset adapter if no previous results or a new search
        if (finalResults == null || previousPageToken == null)
            finalResults = results;
        // Append the results if just going to the next page
        else
            finalResults.addAll(results);

        mAdapter = new VideoListAdapter(finalResults, this);

        mAdapter.setOnBottomReachedListener(new VideoListAdapter.OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                Log.d(TAG, "Reached the bottom");
                videoSearch(mSearchQuery, nextPageToken);
            }
        });

        mVideoList.setAdapter(mAdapter);

        // TODO: adjust scrolling and blinking adapter

        // TODO: there should be a constant with 20?
        if (mAdapter.getItemCount() > 20L)
            mVideoList.scrollToPosition(mAdapter.getItemCount() - 20);

        searchProgressBar.setVisibility(View.INVISIBLE);
        mVideoList.setVisibility(View.VISIBLE);
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
