package com.udacity.study.jam.radiotastic.ui.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.udacity.study.jam.radiotastic.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

@EBean
public class StationRecyclerHelper implements ObservableScrollViewCallbacks {
    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    @RootContext
    protected Activity activity;

    @DimensionPixelSizeRes(R.dimen.flexible_space_image_height)
    protected int mFlexibleSpaceImageHeight;
    @DimensionPixelSizeRes(R.dimen.flexible_space_show_fab_offset)
    protected int mFlexibleSpaceShowFabOffset;
    @DimensionPixelSizeRes(R.dimen.margin_standard)
    protected int mFabMargin;

    private TextView mTitleView;
    private View mOverlayView;
    private Toolbar mToolbar;
    private View mImageView;
    private View mRecyclerViewBackground;
    private ObservableRecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private int mActionBarSize;
    private boolean mFabIsShown;
    private View headerView;
    private View mStateView;

    private String mTitle;
    private View mRoot;

    private final Runnable updateHeaderViewTask = new Runnable() {
        @Override
        public void run() {
            if (headerView != null && headerView.getLayoutParams() != null) {
                headerView.getLayoutParams().height = mFlexibleSpaceImageHeight;
            }
        }
    };

    public View getHeaderView() {
        return headerView;
    }

    public StationRecyclerHelper setup() {
        return this;
    }

    public StationRecyclerHelper rootView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("Root view should not be empty");
        }
        mRoot = view;
        return this;
    }

    public StationRecyclerHelper title(String title) {
        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Title should not be empty");
        }
        mTitle = title;
        return this;
    }

    public void init() {
        ensureDefaults();
        injectViews();
        configure();
    }

    private void ensureDefaults() {
        if (mRoot == null) {
            throw new IllegalStateException("Root view should not be null");
        }
        if (TextUtils.isEmpty(mTitle)) {
            throw new IllegalStateException("Title should not be empty");
        }
    }

    private void injectViews() {
        mTitleView = ((TextView) mRoot.findViewById(R.id.title));
        mOverlayView = mRoot.findViewById(R.id.overlay);
        mToolbar = ((Toolbar) mRoot.findViewById(R.id.toolbar));
        mImageView = mRoot.findViewById(R.id.image);
        mRecyclerViewBackground = mRoot.findViewById(R.id.list_background);
        mRecyclerView = ((ObservableRecyclerView) mRoot.findViewById(R.id.recycler));
        mFab = ((FloatingActionButton) mRoot.findViewById(R.id.fab));
        mStateView = mRoot.findViewById(android.R.id.empty);
    }

    private void configure() {
        mActionBarSize = getActionBarSize();

        mRecyclerView.setScrollViewCallbacks(this);

        headerView = LayoutInflater.from(activity)
                .inflate(R.layout.recycler_header, null);
        headerView.post(updateHeaderViewTask);

        mToolbar.setBackgroundColor(Color.TRANSPARENT);

        mTitleView.setText(mTitle);
        activity.setTitle(null);

        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);

        translateFabButtonOnStart();

        final View contentView = activity.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                // mRecylcerViewBackground's should fill its parent vertically
                // but the height of the content view is 0 on 'onCreate'.
                // So we should get it with post().
                mRecyclerViewBackground.getLayoutParams().height = contentView.getHeight();
            }
        });

        //since you cannot programatically add a headerview to a recyclerview we added an empty view as the header
        // in the adapter and then are shifting the views OnCreateView to compensate
        final float scale = 1 + MAX_TEXT_SCALE_DELTA;
        mRecyclerViewBackground.post(new Runnable() {
            @Override
            public void run() {
                ViewHelper.setTranslationY(mRecyclerViewBackground, mFlexibleSpaceImageHeight);
            }
        });
        ViewHelper.setTranslationY(mOverlayView, mFlexibleSpaceImageHeight);
        mTitleView.post(new Runnable() {
            @Override
            public void run() {
                ViewHelper.setTranslationY(mTitleView, (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale));
                ViewHelper.setPivotX(mTitleView, 0);
                ViewHelper.setPivotY(mTitleView, 0);
                ViewHelper.setScaleX(mTitleView, scale);
                ViewHelper.setScaleY(mTitleView, scale);
            }
        });
    }

    private void translateFabButtonOnStart() {
        mOverlayView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mOverlayView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    removeOnGlobalLayoutListener();
                }
                // Translate FAB
                float fabTranslationY = translateFab(0);
                // Show/hide FAB
                toggleFab(fabTranslationY);
                ViewHelper.setTranslationY(mStateView, mFlexibleSpaceImageHeight + mFabMargin);
            }

            @TargetApi(16)
            private void removeOnGlobalLayoutListener() {
                mOverlayView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Translate list background
        ViewHelper.setTranslationY(mRecyclerViewBackground, Math.max(0, -scrollY + mFlexibleSpaceImageHeight));

        // Change alpha of overlay
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        setPivotXToTitle();
        ViewHelper.setPivotY(mTitleView, 0);
        ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);

        // Translate FAB
        float fabTranslationY = translateFab(scrollY);
        // Show/hide FAB
        toggleFab(fabTranslationY);

        if (scrollY < mFlexibleSpaceImageHeight) {
            ViewHelper.setTranslationY(mToolbar, 0);
        } else {
            ViewHelper.setTranslationY(mToolbar, -scrollY);
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private float translateFab(int scrollY) {
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - mFab.getHeight() / 2,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // On pre-honeycomb, ViewHelper.setTranslationX/Y does not set margin,
            // which causes FAB's OnClickListener not working.
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFab.getLayoutParams();
            lp.leftMargin = mOverlayView.getWidth() - mFabMargin - mFab.getWidth();
            lp.topMargin = (int) fabTranslationY;
            mFab.requestLayout();
        } else {
            ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
            ViewHelper.setTranslationY(mFab, fabTranslationY);
        }
        return fabTranslationY;
    }

    private void toggleFab(float fabTranslationY) {
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }
    }

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = activity.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setPivotXToTitle() {
        Configuration config = activity.getResources().getConfiguration();
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setPivotX(mTitleView, activity.findViewById(android.R.id.content).getWidth());
        } else {
            ViewHelper.setPivotX(mTitleView, 0);
        }
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    public void destroy() {
        headerView.removeCallbacks(updateHeaderViewTask);
    }
}
