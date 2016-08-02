package com.appmobileos.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.spbtv.tele2.BuildConfig;
import com.spbtv.tele2.models.app.BaseUrlHolder;
import com.spbtv.tele2.models.app.HttpParam;
import com.spbtv.tele2.network.INetworkConnectivityStateCallback.NetworkTypes;
import com.spbtv.tele2.util.retrofit.UserAgentHeaderInterceptor;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Response;

import static com.spbtv.tele2.util.BradburyLogger.isDebugGradleBuildEnable;
import static com.spbtv.tele2.util.BradburyLogger.logDebug;
import static com.spbtv.tele2.util.BradburyLogger.makeLogTag;

/**
 * Created by Andrey Nikonov on 16.03.16.
 * Utils related with network
 */
public class NetworkUtil {
    private static final String LOG_DEBUG = makeLogTag(NetworkUtil.class);

    private static NetworkTypes sNetworkType;
    private static NetworkTypes sLastNetworkType;

    /**
     * Create user agent for connect Bradbury CMS
     *
     * @return userAgent
     */
    public static String createBradburyUserAgent() {
        StringBuilder userAgentBuilder = new StringBuilder();
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer) || manufacturer.indexOf('\0') != -1) {
            manufacturer = "unknown manufacture";
        }
        String product = Build.PRODUCT;
        if (TextUtils.isEmpty(product) || product.indexOf('\0') != -1) {
            product = "unknown product";
        }

        String model = Build.MODEL;
        if (TextUtils.isEmpty(model) || model.indexOf('\0') != -1) {
            manufacturer = "unknown model";
        }

        userAgentBuilder.append("BradburyLab (")
                .append(manufacturer)
                .append("_")
                .append(product)
                .append("; ")
                .append(model)
                .append("; ")
                .append("Android")
                .append("; ")
                .append(Build.VERSION.RELEASE)
                .append(") ")
                .append(BuildConfig.VERSION_NAME)
                .append(" (")
                .append(BuildConfig.VERSION_CODE)
                .append(")");
        return userAgentBuilder.toString();
    }

    public static OkHttpClient createDefaultClient() {
        return createOkHttpClient(10, 10, isDebugGradleBuildEnable());
    }


    /**
     * Creates http client
     *
     * @param readTimeout       read timeout in seconds
     * @param connectionTimeout connection timeout in seconds
     * @param enableLogging     if true logging request and response
     */
    public static OkHttpClient createOkHttpClient(int readTimeout, int connectionTimeout, boolean enableLogging) {
        if (readTimeout <= 0) {
            throw new IllegalArgumentException(" readTimeout must be > 0 ");
        }
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException(" connectionTimeout must be > 0 ");
        }
        //http client
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setReadTimeout(readTimeout, TimeUnit.SECONDS);
        httpClient.setConnectTimeout(connectionTimeout, TimeUnit.SECONDS);
        httpClient.networkInterceptors().add(new UserAgentHeaderInterceptor
                (NetworkUtil.createBradburyUserAgent()));
        if (enableLogging) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            //logging
            httpClient.interceptors().add(logging);
        }
        logDebug(LOG_DEBUG, "createOkHttpClient() called with: " +
                "readTimeout = [" + readTimeout + "], connectionTimeout = [" + connectionTimeout + "], enableLogging = [" + enableLogging + "]");
        return httpClient;
    }

    /**
     * Creates http client
     *
     * @param readTimeout       read timeout in seconds
     * @param connectionTimeout connection timeout in seconds
     * @param enableLogging     if true logging request and response
     */
    public static OkHttpClient createOkHttpClient(int readTimeout, int connectionTimeout, final boolean enableLogging, final List<HttpParam> params) {
        OkHttpClient httpClient = createOkHttpClient(readTimeout, connectionTimeout, enableLogging);
        httpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.httpUrl();

                HttpUrl.Builder builder = originalHttpUrl.newBuilder();
                for (HttpParam param : params) {
                    if (enableLogging) logDebug(LOG_DEBUG, " Added: " + param);
                    builder.addQueryParameter(param.getName(), param.getValue());
                }
                Request.Builder requestBuilder = original.newBuilder()
                        .url(builder.build());
                Request request = requestBuilder.build();
                if (enableLogging)
                    logDebug(LOG_DEBUG, " Final request url: " + request.urlString());
                return chain.proceed(request);
            }
        });
        return httpClient;
    }

    public static BaseUrlHolder createBaseUrlHolder() {
        return new BaseUrlHolder(BuildConfig.URL_SCREEN, BuildConfig.URL_AUTH, BuildConfig.URL_LIVES, BuildConfig.URL_LIKES, BuildConfig.URL_VANGA);
    }

    public static NetworkTypes getNetworkType() {
        return sNetworkType;
    }

    public static void setNetworkType(NetworkTypes networkType) {
        sLastNetworkType = sNetworkType;
        sNetworkType = networkType;
    }

    public static boolean networkWasChanged() {
        return sLastNetworkType != sNetworkType;
    }

    public static boolean isConnected(Context context) {
        if (context == null) {
            return true;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
        return nwInfo != null && nwInfo.isConnectedOrConnecting();
    }

    public static boolean isConnectingException(Throwable throwable) {
        return throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException;
    }


    public static long parseCacheControl(Response response) {
        if (response != null) {
            com.squareup.okhttp.Response raw = response.raw();
            if (raw != null) {
                String headerValue = raw.header("Cache-Control");
                if (headerValue != null) {
                    String[] tokens = headerValue.split(",");
                    for (String token : tokens) {
                        String finalToken = token.trim();
                        if (finalToken.equals("no-cache") || finalToken.equals("no-store")) {
                            return 0;
                        } else if (finalToken.startsWith("max-age=")) {
                            try {
                                return Long.parseLong(finalToken.substring(8));
                            } catch (Exception e) {
                                e.printStackTrace();
                                CrashlyticsUtil.sendException(e);
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static long parseCacheControl(Headers headers) {
        if (headers != null) {
            String headerValue = headers.get("Cache-Control");
            if (headerValue != null) {
                String[] tokens = headerValue.split(",");
                for (String token : tokens) {
                    String finalToken = token.trim();
                    if (finalToken.equals("no-cache") || finalToken.equals("no-store")) {
                        return 0;
                    } else if (finalToken.startsWith("max-age=")) {
                        try {
                            return Long.parseLong(finalToken.substring(8));
                        } catch (Exception e) {
                            e.printStackTrace();
                            CrashlyticsUtil.sendException(e);
                        }
                    }
                }
            }

        }
        return 0;
    }

}


