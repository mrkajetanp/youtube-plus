package com.cajetan.youtubeplus;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
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
    private YouTubeData mYouTubeData;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        mYouTubeData = new YouTubeData(this);
        mContext = this;

        setupBottomBar();

        mFavouriteList = findViewById(R.id.favourite_list);
        mFavouriteList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new FavouriteListAdapter(Collections.<Video>emptyList(), this, this);
        mFavouriteList.setHasFixedSize(false);
        mFavouriteList.setAdapter(mAdapter);

        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel.class);
        mVideoDataViewModel.getAllVideoData().observe(this, new Observer<List<VideoData>>() {
            @Override
            public void onChanged(@Nullable List<VideoData> videoData) {
                mYouTubeData.receiveFavouritesResults(videoData);
            }
        });

//        mYouTubeData.receiveFavouritesResults(mVideoDataViewModel.getAllVideoData().getValue());
    }

     private void setupBottomBar() {
        mBottomNavBar = findViewById(R.id.bottom_bar);
        mBottomNavBar.setSelectedItemId(R.id.action_favourites);
        mBottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.action_start) {
                    finish();
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_favourites) {
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_others) {
//                    item.setChecked(true);
                    return true;
                }

                return false;
            }
        });
    }

    // TODO: loading bar like in other activities

    @Override
    public void onFavouritesReceived(List<Video> results) {
        mAdapter.clearItems();
        mAdapter.addItems(results);
    }

    @Override
    public void onListItemClick(String clickedVideoId) {
        Toast.makeText(this, "List item clicked", Toast.LENGTH_LONG).show();
    }

     // TODO: quite easy to omit, look for better solutions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
