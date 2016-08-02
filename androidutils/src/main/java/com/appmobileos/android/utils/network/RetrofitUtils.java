package com.appmobileos.android.utils.network;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spbtv.tele2.models.app.BaseUrlHolder;
import com.spbtv.tele2.models.app.Error;
import com.spbtv.tele2.models.app.IndentError;
import com.spbtv.tele2.models.bradbury.PageScreen;
import com.spbtv.tele2.network.IviServiceApi;
import com.spbtv.tele2.util.BradburyLogger;
import com.spbtv.tele2.util.NetworkUtil;
import com.spbtv.tele2.util.gson.PageScreenDeserializer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

import static com.spbtv.tele2.util.BradburyLogger.isDebugGradleBuildEnable;

public class RetrofitUtils {
    public static Map<String, Retrofit> getBradburyRetrofitMap(BaseUrlHolder urlHolder, String installId) {
        Map<String, Retrofit> mBradburyRetrofitMap = new HashMap<>(5);
        //base screen retrofit
        Gson gsonScreen = new GsonBuilder().registerTypeAdapter(PageScreen.class,
                new PageScreenDeserializer()).create();
        Retrofit screen = createRetrofitBradburyHeaders(urlHolder.getScreenUrl(), gsonScreen, installId);
        mBradburyRetrofitMap.put(urlHolder.getScreenUrl(), screen);
        //base authorization retrofit
        Retrofit authorization = createRetrofitBradburyHeaders(urlHolder.getTele2Url(), installId);
        mBradburyRetrofitMap.put(urlHolder.getTele2Url(), authorization);
        //base epg retrofit
        Retrofit epg = createRetrofitBradburyHeaders(urlHolder.getEpgUrl(), installId);
        mBradburyRetrofitMap.put(urlHolder.getEpgUrl(), epg);
        //base like retrofit
        Retrofit like = createRetrofitBradburyHeaders(urlHolder.getLikeUrl(), installId);
        mBradburyRetrofitMap.put(urlHolder.getLikeUrl(), like);//base like retrofit
        Retrofit vanga = createRetrofitBradburyHeaders(urlHolder.getVangaUrl(), installId);
        mBradburyRetrofitMap.put(urlHolder.getVangaUrl(), vanga);
        return mBradburyRetrofitMap;
    }

    public static Retrofit createRetrofit(String baseUrl) {
        return createRetrofit(baseUrl, new Gson());
    }

    public static Retrofit createRetrofit(String baseUrl, Gson gson) {
        return createRetrofit(baseUrl, gson, NetworkUtil.createOkHttpClient(10, 10, isDebugGradleBuildEnable()));
    }

    public static Retrofit createRetrofitBradburyHeaders(String baseUrl, String iid) {
        return createRetrofitBradburyHeaders(baseUrl, new Gson(), iid);
    }

    public static Retrofit createRetrofitBradburyHeaders(String baseUrl, Gson gson, String iid) {
        //user agent default header already added
        OkHttpClient client = NetworkUtil.createDefaultClient();
        if (iid == null) iid = "";
        //add header install id
        client.networkInterceptors().add(new InstallIdHeaderInterceptor(iid));
        return createRetrofit(baseUrl, gson, client);
    }

    public static Retrofit createRetrofit(@NonNull String baseUrl, @NonNull Gson gson, @NonNull OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
    }

    public static IviServiceApi getIviServiceApi(String baseUrl) {
        Retrofit mIviRetrofit = createRetrofit(baseUrl);
        return mIviRetrofit.create(IviServiceApi.class);
    }

    public static IviServiceApi getIviServiceApi(String baseUrl, OkHttpClient client) {
        Retrofit mIviRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
        return mIviRetrofit.create(IviServiceApi.class);
    }


    public static Error parseError(Response<?> response, int statusCode, Retrofit retrofit) {
        Converter<ResponseBody, Error> errorConverter =
                retrofit.responseConverter(Error.class, new Annotation[0]);

        Error error = null;

        try {
            error = errorConverter.convert(response.errorBody());
        } catch (IOException e) {
            BradburyLogger.logError("RetrofitUtils", "Can't convert response.errorBody() to error");
        }
        if (error == null) {
            error = new Error();
        }
        error.setStatusCode(statusCode);
        return error;
    }

    public static Error parseIndentError(String json, int statusCode) {
        Error error = new Error();
        if (statusCode == Error.PAYMENT_REQUIRED_ERROR || statusCode == Error.SOMETHING_WRONG_ERROR) {
            IndentError indentError = new Gson().fromJson(json, IndentError.class);
            error.setError(indentError.getError());
        }
        error.setStatusCode(statusCode);
        return error;
    }

}
