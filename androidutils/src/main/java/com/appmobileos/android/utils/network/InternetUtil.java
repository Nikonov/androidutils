package com.appmobileos.android.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.appmobileos.android.utils.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by anikonov on 4/1/14.
 */
public class InternetUtil {
    private static final String TAG = "InternetUtil";
    private static final String BOUNDARY = "w23renff491nc4rth56u349";
    private static final int MAX_SIZE_ARCHIVE = 5 * 1024 * 1024;//5mb
    private static final String ANDROID_AGENT = " Android " + Build.VERSION.CODENAME;

    public static String createBlocksMultiPartType(Map<String, Object> keyAndValuePostData) {
        if (keyAndValuePostData == null) {
            throw new NullPointerException("keyAndValuePostData must !=null");
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> param : keyAndValuePostData.entrySet()) {
            builder.append("--").append(BOUNDARY).append("\r\n");
            String key = param.getKey();
            Object value = param.getValue();
            if (key != null && value != null) {
                if (value instanceof String) {
                    builder.append(cteateTextBlockMultiPartType(key, (String) value));
                } else if (value instanceof File) {
                    builder.append(createFileBlockMultiPartType((File) value, key));
                } else {
                    try {
                        String object = URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8");
                        builder.append(cteateTextBlockMultiPartType(key, object));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return builder.toString();
    }

    public static String createFileBlockMultiPartType(File fileSend, String nameParam) {
        if (BuildConfig.DEBUG) Log.i(TAG, "FILE NAME SEND  = " + fileSend.getName());
        StringBuilder builder = new StringBuilder();
        builder.append("--" + BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"" + nameParam + "\"; filename=\"" + fileSend.getName() + "\"").append("\r\n")
                .append("Content-Type: application/octet-stream").append("\r\n")
                .append("Content-Transfer-Encoding: binary").append("\r\n\r\n");
        return builder.toString();
    }

    public static String createFileBlockMultiPartType(Map<String, File> sendFile) {
        StringBuilder builder = new StringBuilder();
        String key = sendFile.keySet().iterator().next();
        File value = sendFile.values().iterator().next();
        builder.append("--" + BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + value.getName() + "\"").append("\r\n")
                .append("Content-Type: application/octet-stream").append("\r\n")
                .append("Content-Transfer-Encoding: binary").append("\r\n\r\n");
        return builder.toString();
    }

    public static String createTextBlockMultiPartType(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> param : data.entrySet()) {
            builder.append("--" + BOUNDARY).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"").append("\r\n")
                    .append("Content-Type: application/x-www-form-urlencoded").append("\r\n")
                    .append("Content-Transfer-Encoding: 8bit").append("\r\n\r\n")
                    .append(param.getValue()).append("\r\n");
        }
        return builder.toString();
    }

    public static String cteateTextBlockMultiPartType(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("--" + BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"" + key + "\"").append("\r\n")
                .append("Content-Type: application/x-www-form-urlencoded").append("\r\n")
                .append("Content-Transfer-Encoding: 8bit").append("\r\n\r\n")
                .append(value).append("\r\n");
        return builder.toString();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String convertPostData(Map<String, Object> params) {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append("&");
            try {
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append("=");
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return postData.toString();
    }

    public static int sendDataInServer(String address, boolean doInput, Map<String, Object> postData, Map<String, File> fileSend) throws IOException {
        if (address == null) {
            throw new NullPointerException("Address must !=null. Now address = null ");
        }
        if (postData == null) {
            throw new NullPointerException("PostData must !=null. Now PostData = null ");
        }
        if (fileSend == null) {
            sendDataInServer(address, doInput, postData);
        } else if (fileSend.size() > 1) {
            throw new IllegalArgumentException("Support one row in collection. Now size ==" + fileSend.size());
        }
        URL ulr = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) ulr.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(doInput);
        connection.setUseCaches(false);
        String firstBlockParams = createTextBlockMultiPartType(postData);
        String secondBlockFile = createFileBlockMultiPartType(fileSend);
        String endBlock = createEndBlockPost();
        File sendFile = fileSend.values().iterator().next();
        int bytesAvailable, bufferSize;
        BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(sendFile));
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, MAX_SIZE_ARCHIVE);
        connection.setRequestProperty("User-Agent", ANDROID_AGENT);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        connection.setRequestProperty("Content-Length", String.valueOf(firstBlockParams.getBytes("UTF-8").length + secondBlockFile.length() + bufferSize + endBlock.getBytes("UTF-8").length));
        connection.connect();
        BufferedOutputStream dataOutputStream = new BufferedOutputStream(connection.getOutputStream());
        //write firstBlockParams
        dataOutputStream.write(firstBlockParams.getBytes("UTF-8"));
        //write start block
        dataOutputStream.write(secondBlockFile.getBytes());
        byte[] buffer = new byte[bufferSize];
        int bufferRead = fileInputStream.read(buffer, 0, bufferSize);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "FINISH REQUEST = " + firstBlockParams + secondBlockFile + endBlock + "\n" + " SIZE = " + String.valueOf(firstBlockParams.getBytes().length +
                    secondBlockFile.length() + bufferSize + endBlock.length()));
        }
        dataOutputStream.write(buffer, 0, bufferRead);
        //write end block
        dataOutputStream.write(endBlock.getBytes("UTF-8"));
        fileInputStream.close();
        dataOutputStream.flush();
        dataOutputStream.close();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    private static String createEndBlockPost() {
        return "\r\n--" + BOUNDARY + "--\r\n";
    }

    public static int sendDataInServer(String address, boolean doInput, Map<String, Object> postData) throws IOException {
        if (address == null) {
            throw new NullPointerException("Address must !=null. Now address = null ");
        }
        if (postData == null) {
            throw new NullPointerException("PostData must !=null. Now PostData = null ");
        }
        URL url = new URL(address);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(doInput);
        urlConnection.setUseCaches(false);
        String sendData = convertPostData(postData);
        urlConnection.setRequestProperty("User-Agenaddresaddt", ANDROID_AGENT);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Transfer-Encoding:", "8bit");
        urlConnection.setRequestProperty("Content-Length", String.valueOf(sendData.getBytes("UTF-8").length));
        urlConnection.connect();
        OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
        outputStream.write(sendData.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
        int responseCode = urlConnection.getResponseCode();
        urlConnection.disconnect();
        return responseCode;
    }
}
