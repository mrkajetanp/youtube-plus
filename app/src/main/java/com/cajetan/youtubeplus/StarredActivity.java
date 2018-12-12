package com.cajetan.youtubeplus;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StarredActivity extends AppCompatActivity {

    private static final String TAG = StarredActivity.class.getSimpleName();

    private BottomNavigationView mBottomNavBar;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred);

        mContext = this;

        setupBottomBar();
    }

     private void setupBottomBar() {
        mBottomNavBar = findViewById(R.id.bottom_bar);
        mBottomNavBar.setSelectedItemId(R.id.action_starred);
        mBottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.action_start) {
                    finish();
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_others) {
                    Log.d(TAG, "Others not implemented yet");
                    item.setChecked(true);
                    return true;
                }

                if (item.getItemId() == R.id.action_starred) {
                    item.setChecked(true);
                    return true;
                }

                return false;
            }
        });
    }

}
