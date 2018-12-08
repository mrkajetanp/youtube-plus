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
import com.google.api.services.youtube.model.Video;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements YouTubeData.VideoSearchListener, VideoListAdapter.ListItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText searchBox;
    private VideoListAdapter mAdapter;
    private RecyclerView mVideoList;

    private ProgressBar searchProgressBarCentre;
    private ProgressBar searchProgressBarBottom;

    private YouTubeData mYouTubeData;

    private String mSearchQuery = null;
    private String mNextPageToken = "";

    // TODO: implement auto fullscreen on rotation

    /*//////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////////////////////*/

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

                    videoSearch(null);
                    handled = true;
                }

                return handled;
            }
        });

        mVideoList = findViewById(R.id.search_results);
        searchProgressBarCentre = findViewById(R.id.search_progress_bar_centre);
        searchProgressBarBottom = findViewById(R.id.search_progress_bar_bottom);

        mVideoList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new VideoListAdapter(Collections.<Video>emptyList(), this, this);
        mAdapter.setOnBottomReachedListener(new VideoListAdapter.OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                Log.d(TAG, "Reached the bottom");
                videoSearch(mNextPageToken);
            }
        });

        mVideoList.setHasFixedSize(false);
        mVideoList.setAdapter(mAdapter);

        createNotificationChannel();
    }

    // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////////*/

    public void videoSearch(String nextPageToken) {
        if (nextPageToken == null) {
            searchProgressBarCentre.setVisibility(View.VISIBLE);
            mVideoList.setVisibility(View.INVISIBLE);
        } else {
            searchProgressBarBottom.setVisibility(View.VISIBLE);
        }

        Log.d("YouTubeData", "Searching for a video: " + searchBox.getText().toString());
        mYouTubeData.receiveSearchResults(mSearchQuery, nextPageToken);
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


    /*//////////////////////////////////////////////////////////////////////////////
    // Callbacks and others
    //////////////////////////////////////////////////////////////////////////////*/

    // TODO: another callback for new page results
    @Override
    public void onSearchResultsReceived(List<Video> results,
                                        final String nextPageToken, String previousPageToken) {
        //  Reset the adapter if no previous results or a new search
        if (previousPageToken == null || previousPageToken.equals("")) {
            mAdapter.clearItems();
            mVideoList.scrollToPosition(0);

            searchProgressBarCentre.setVisibility(View.INVISIBLE);
            mVideoList.setVisibility(View.VISIBLE);
        } else {
            searchProgressBarBottom.setVisibility(View.GONE);
        }

        mAdapter.addItems(results);
        mNextPageToken = nextPageToken;
    }

    @Override
    public void onListItemClick(String clickedVideoId) {
        Intent videoPlayerIntent = new Intent(this, PlayerActivity.class);
        videoPlayerIntent.putExtra(getString(R.string.video_id_key), clickedVideoId);
        startActivity(videoPlayerIntent);
    }
}
