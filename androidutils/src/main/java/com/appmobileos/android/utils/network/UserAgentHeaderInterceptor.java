package com.appmobileos.android.utils.network;

import android.text.TextUtils;

import com.spbtv.tele2.util.NetworkUtil;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import static com.spbtv.tele2.util.BradburyLogger.logDebug;
import static com.spbtv.tele2.util.BradburyLogger.makeLogTag;

/**
 * Created by Andrey Nikonov on 06.06.16.
 */
public class UserAgentHeaderInterceptor implements Interceptor {
    private static final String LOG_DEBUG = makeLogTag(UserAgentHeaderInterceptor.class);
    private String mUserAgent;

    public UserAgentHeaderInterceptor(String userAgent) {
        this.mUserAgent = userAgent;
        if (TextUtils.isEmpty(mUserAgent)) {
            mUserAgent = NetworkUtil.createBradburyUserAgent();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", mUserAgent)
                .build();
        logDebug(LOG_DEBUG, "intercept called mUserAgent: " + mUserAgent);
        return chain.proceed(requestWithUserAgent);
    }
}
