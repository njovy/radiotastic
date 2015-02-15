/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.udacity.study.jam.radiotastic.station.StationListFragment;


public class StationsActivity extends ActionBarActivity {
    public static String CATEGORY_ID_EXTRA = "categoryID";
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        if (getIntent() != null && getIntent().hasExtra(CATEGORY_ID_EXTRA)) {
            categoryId = getIntent().getIntExtra(CATEGORY_ID_EXTRA, 0);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, StationListFragment.init(categoryId))
                    .commit();
        }
    }
}
