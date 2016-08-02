package com.appmobileos.android.utils;

import rx.Subscription;

/**
 * Created by Andrey Nikonov on 14.03.16.
 */
public class RxUtils {
    /**
     * Unsubscribe a {@link Subscription} if need
     *
     * @param subscription The subscription will unsubscribe if need, may be null
     */
    public static void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
