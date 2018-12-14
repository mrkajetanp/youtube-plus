package com.cajetan.youtubeplus;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.cajetan.youtubeplus.data.VideoData;
import com.cajetan.youtubeplus.data.VideoDataViewModel;

import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private static final String TAG = FavouritesActivity.class.getSimpleName();

    private VideoDataViewModel mVideoDataViewModel;

    private BottomNavigationView mBottomNavBar;

    private TextView mPlaceholderView;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        mPlaceholderView = findViewById(R.id.placeholder);

        mContext = this;

        setupBottomBar();

        mVideoDataViewModel = ViewModelProviders.of(this).get(VideoDataViewModel.class);

        mVideoDataViewModel.getAllVideoData().observe(this, new Observer<List<VideoData>>() {
            @Override
            public void onChanged(@Nullable List<VideoData> videoData) {
                if (videoData != null && videoData.size() > 0) {
                    StringBuilder result = new StringBuilder();

                    for (VideoData data : videoData) {
                        result.append(data.getVideoId());
                        result.append('\n');
                    }

                    mPlaceholderView.setText(result.toString());
                }
            }
        });

        mVideoDataViewModel.delete(new VideoData("test_id"));
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

}
