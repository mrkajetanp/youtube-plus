package com.cajetan.youtubeplus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.cajetan.youtubeplus.utils.YouTubeData;
import com.google.api.services.youtube.model.Video;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements YouTubeData.VideoSearchListener, VideoListAdapter.ListItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoListAdapter mAdapter;
    private RecyclerView mVideoList;

    private BottomNavigationView mBottomNavBar;

    private ProgressBar searchProgressBarCentre;
    private ProgressBar searchProgressBarBottom;

    private YouTubeData mYouTubeData;

    private String mSearchQuery = null;
    private String mNextPageToken = "";

    private Context mContext;


    // TODO: implement auto fullscreen on rotation

    /*//////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mYouTubeData = new YouTubeData(this);

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

        mContext = this;

        createNotificationChannel();
        setupBottomBar();

        handleIntent(getIntent());
    }

    // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBottomNavBar != null)
            mBottomNavBar.setSelectedItemId(R.id.action_start);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
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

        Log.d("YouTubeData", "Searching for a video: " + mSearchQuery);
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

    private void handleIntent(Intent intent) {
        if (intent == null)
            return;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            if (query.equals("") || query.equals(mSearchQuery))
                return;

            mSearchQuery = query;
            videoSearch(null);
        }
    }

    private void setupBottomBar() {
        mBottomNavBar = findViewById(R.id.bottom_bar);
        mBottomNavBar.setSelectedItemId(R.id.action_start);
        mBottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.action_start) {
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_others) {
                    Log.d(TAG, "Others not implemented yet");
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_favourites) {
                    startActivity(new Intent(mContext, FavouritesActivity.class));
                    return true;
                }

                return false;
            }
        });
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
