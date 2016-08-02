package com.appmobileos.android.utils;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Andrey Nikonov on 22.05.16.
 * Helper class which manager fragment back stack
 */
public abstract class BackStackAbstract {
    private int mCounter = 0;

    private List<String> mTags = new ArrayList<>();

    public void resetRootTagSystemBackStack(@NonNull FragmentManager manager) {
        checkNotNull(manager, "manager");
        for (int i = 0; i < mTags.size(); i++) {
            final String tag = mTags.get(i);
            if (i != 0) {
                manager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        manager.executePendingTransactions();
        String rootTag = getRootTag();
        reset();
        mTags.add(rootTag);
        mCounter++;
    }

    public void resetSystemBackStack(@NonNull FragmentManager manager) {
        checkNotNull(manager, "manager");
        for (String tag : mTags) {
            manager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        manager.executePendingTransactions();
        reset();
    }

    public String getRootTag() {
        if (!mTags.isEmpty()) {
            return mTags.get(0);
        }
        return "";
    }

    public List<String> getBackStackTags() {
        return mTags;
    }

    public boolean hasBackStack() {
        return mTags.size() != 0;
    }

    public boolean hasOnlyRoot() {
        return mTags.size() == 1;
    }

    public void reset() {
        mTags.clear();
        mCounter = 0;
    }

    public boolean isEmpty() {
        return mTags.size() == 0;
    }

    public String makeUniqueTag(String tag) {
        mCounter++;
        return tag.concat(Integer.toString(mCounter));
    }

    public void incrementBackStack(String tag) {
        mTags.add(tag);
    }

    public void decrementBackStack(@NonNull FragmentManager manager) {
        manager.popBackStack();
        if (!mTags.isEmpty()) {
            mTags.remove(mTags.size() - 1);
        }
    }
}
