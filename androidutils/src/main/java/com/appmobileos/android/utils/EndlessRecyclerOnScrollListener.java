package com.appmobileos.android.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Andrey Nikonov on 14.03.16.
 * Copy past from
 * <a href="https://gist.github.com/ssinss/e06f12ef66c51252563e#file-endlessrecycleronscrolllistener-java">EndlessRecyclerOnScrollListener</>
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();
    private static final int FIRST_PAGE = 1;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = FIRST_PAGE;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (NetworkUtil.networkWasChanged()) {
                loading = false;
                previousTotal = totalItemCount;
            } else {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached
            final boolean firstPage = current_page == FIRST_PAGE;
            // Do something
            current_page++;

            onLoadMore(current_page);

            onLoadMore(current_page, firstPage);

            loading = true;
        }
    }

    protected void onLoadMore(int currentPage) {
        //sub classes can override it
    }

    protected void onLoadMore(int currentPage, boolean firstPage) {
        //sub classes can override it
    }


    /**
     * Clear all fields
     * Lazy load start with first page
     */
    public void reset() {
        current_page = 1;
        previousTotal = 0;
        loading = true;
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
    }
}
