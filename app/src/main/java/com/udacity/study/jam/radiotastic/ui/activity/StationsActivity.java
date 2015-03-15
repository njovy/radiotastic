/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.udacity.study.jam.radiotastic.R;
import com.udacity.study.jam.radiotastic.ui.fragment.StationListFragment;


public class StationsActivity extends ActionBarActivity implements StationListFragment.Callback {

    public static String CATEGORY_ID_EXTRA = "categoryID";

    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        if (getIntent() != null && getIntent().hasExtra(CATEGORY_ID_EXTRA)) {
            categoryId = getIntent().getStringExtra(CATEGORY_ID_EXTRA);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, StationListFragment.init(categoryId))
                    .commit();
        }
    }

    @Override
    public void onStationSelected(String stationID, String streamUrl) {
        Intent newIntent = new Intent(this, DetailActivity.class);
        newIntent.putExtra(DetailActivity.STATION_ID_EXTRA, stationID);
        newIntent.putExtra(DetailActivity.STATION_STREAM_URL_EXTRA, streamUrl);
        startActivity(newIntent);
    }

}