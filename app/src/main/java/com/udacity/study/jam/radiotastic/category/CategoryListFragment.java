/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.category;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.udacity.study.jam.radiotastic.ApplicationComponent;
import com.udacity.study.jam.radiotastic.R;
import com.udacity.study.jam.radiotastic.db.category.CategoryColumns;
import com.udacity.study.jam.radiotastic.db.category.CategoryCursor;
import com.udacity.study.jam.radiotastic.domain.ImmediateSyncCase;
import com.udacity.study.jam.radiotastic.domain.ObserveSyncStateCase;
import com.udacity.study.jam.radiotastic.util.SimpleOnItemTouchListener;
import com.udacity.study.jam.radiotastic.widget.DataImageView;

import javax.inject.Inject;

public class CategoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOAD_CATEGORIES = 100;

    private RecyclerView recyclerView;
    private GestureDetectorCompat gestureDetectorCompat;
    private CategoryAdapter mAdapter;
    private DataImageView emptyImageView;
    private ProgressBar progressBar;

    @Inject
    ImmediateSyncCase immediateSync;
    @Inject
    ObserveSyncStateCase observeSyncCase;

    private boolean mSyncIsActive;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_category_list, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        emptyImageView = (DataImageView) root.findViewById(android.R.id.empty);
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refreshLayout);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_dark,
                R.color.holo_yellow_dark,
                R.color.holo_green_dark,
                R.color.holo_purple_dark,
                R.color.holo_red_dark
        );

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                immediateSync.start(null);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ApplicationComponent.Initializer.init(getActivity()).inject(this);

        observeSyncCase.create(new ObserveSyncStateCase.SyncStatusCallBack() {
            @Override
            public void onStatusChanged(boolean syncIsActive) {
                mSyncIsActive = syncIsActive;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter.getCursor() == null) {
                            emptyImageView.setImageType(
                                    mSyncIsActive
                                            ? DataImageView.Type.SYNC
                                            : DataImageView.Type.NONE);
                        }
                        swipeRefreshLayout.setRefreshing(mSyncIsActive);
                    }
                });
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        // actually VERTICAL is the default,
        // just remember: LinearLayoutManager
        // supports HORIZONTAL layout out of the box
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // you can set the first visible item like this:
        linearLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(linearLayoutManager);

        // you can set the first visible item like this:
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnItemTouchListener(new ItemTouchListener());

        gestureDetectorCompat = new GestureDetectorCompat(getActivity(),
                new RecyclerViewGestureListener());

        mAdapter = new CategoryAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(LOAD_CATEGORIES, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        observeSyncCase.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        observeSyncCase.resume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), CategoryColumns.CONTENT_URI,
                CategoryColumns.ALL_COLUMNS, null, null, CategoryColumns.NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            immediateSync.start(null);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mSyncIsActive) {
                        emptyImageView.setImageType(DataImageView.Type.EMPTY);
                    }
                }
            });
        } else {
            emptyImageView.setImageType(DataImageView.Type.NONE);
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class ItemTouchListener extends SimpleOnItemTouchListener {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            gestureDetectorCompat.onTouchEvent(motionEvent);
            return false;
        }
    }

    private class RecyclerViewGestureListener
            extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
            int position = recyclerView.getChildPosition(view);
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            CategoryCursor categoryCursor = new CategoryCursor(cursor);
            Toast.makeText(getActivity(), "Selected " + categoryCursor.getCategoryId(),
                    Toast.LENGTH_SHORT).show();
//            CategoryItem categoryItem = mAdapter.getItem(position);
//            if (getActivity() instanceof Callback) {
//                ((Callback) getActivity()).onCategorySelected(categoryItem.getId());
//            }
            return super.onSingleTapConfirmed(event);
        }
    }

    public static interface Callback {
        void onCategorySelected(double categoryID);
    }
}
