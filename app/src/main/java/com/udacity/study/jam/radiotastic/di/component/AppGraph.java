/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.di.component;

import com.udacity.study.jam.radiotastic.sync.internal.StationSyncService;
import com.udacity.study.jam.radiotastic.ui.activity.MainActivity;
import com.udacity.study.jam.radiotastic.ui.presenter.CategoriesPresenter;
import com.udacity.study.jam.radiotastic.ui.presenter.StationsPresenter;

public interface AppGraph {
    void inject(MainActivity activity);
    void inject(CategoriesPresenter presenter);
    void inject(StationsPresenter presenter);
    void inject(StationSyncService stationSyncService);
}
