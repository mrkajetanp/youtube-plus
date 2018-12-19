package com.cajetan.youtubeplus;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cajetan.youtubeplus.data.VideoData;
import com.cajetan.youtubeplus.data.VideoDataViewModel;
import com.cajetan.youtubeplus.utils.YouTubeData;
import com.google.api.services.youtube.model.Video;

import java.util.Collections;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity
        implements YouTubeData.FavouritesDataListener, FavouriteListAdapter.ListItemClickListener {

    private static final String TAG = FavouritesActivity.class.getSimpleName();

    private VideoDataViewModel mVideoDataViewModel;

    private RecyclerView mFavouriteList;
    private FavouriteListAdapter mAdapter;

    private BottomNavigationView mBottomNavBar;
    private ProgressBar mProgressBarCentre;
    private YouTubeData mYouTubeData;

    // TODO: caching results?

    /*//////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        mYouTubeData = new YouTubeData(this);
        mProgressBarCentre = findViewById(R.id.progress_bar_centre);

        setupFavouritesList();
        setupDatabase();
        setupBottomBar();
    }

    // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////////*/

    private void setupFavouritesList() {
        mFavouriteList = findViewById(R.id.favourite_list);
        mFavouriteList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new FavouriteListAdapter(Collections.<Video>emptyList(), this, this);
        mFavouriteList.setHasFixedSize(false);
        mFavouriteList.setAdapter(mAdapter);
    }

    private void setupDatabase() {
        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel.class);
        mVideoDataViewModel.getAllVideoData().observe(this, new Observer<List<VideoData>>() {
            @Override
            public void onChanged(@Nullable List<VideoData> videoData) {
                loadFavourites(videoData);
            }
        });
    }

    private void setupBottomBar() {
        mBottomNavBar = findViewById(R.id.bottom_bar);
        mBottomNavBar.setSelectedItemId(R.id.action_favourites);
        mBottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_start) {
                    finish();
                    item.setChecked(true);
                    return true;
                }

                if (id == R.id.action_favourites) {
                    item.setChecked(true);
                    return true;
                }

                if (id == R.id.action_others) {
//                    item.setChecked(true);
                    return true;
                }

                return false;
            }
        });
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////////*/

    private void loadFavourites(List<VideoData> videoData) {
        mFavouriteList.setVisibility(View.INVISIBLE);
        mProgressBarCentre.setVisibility(View.VISIBLE);

        mYouTubeData.receiveFavouritesResults(videoData);
    }

    /*//////////////////////////////////////////////////////////////////////////////
    // Callbacks and others
    //////////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onFavouritesReceived(List<Video> results) {
        mAdapter.clearItems();
        mAdapter.addItems(results);

        mProgressBarCentre.setVisibility(View.INVISIBLE);
        mFavouriteList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListItemClick(String clickedVideoId) {
        Intent videoPlayerIntent = new Intent(this, PlayerActivity.class);
        videoPlayerIntent.putExtra(getString(R.string.video_id_key), clickedVideoId);
        startActivity(videoPlayerIntent);
    }

    @Override
    public void onListItemLongClick(String clickedVideoId) {
        final String videoId = clickedVideoId;

        new AlertDialog.Builder(this)
                .setMessage("Do you really want to remove this video from favourites?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mVideoDataViewModel.delete(new VideoData(videoId));
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }
}
