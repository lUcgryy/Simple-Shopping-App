package com.example.simple.utils;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpUtils {
    private static final String BASE_URL = "http://10.20.22.126:3443";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String ipAddress, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }
    
    public static void post(String ipAddress, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void put(String ipAddress, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void patch(String ipAddress, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.patch(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void delete(String ipAddress, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void get(String ipAddress, String url, String token, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer " + token);
        client.get(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void post(String ipAddress, String url, String token, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer " + token);
        client.post(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void put(String ipAddress, String url, String token, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer " + token);
        client.put(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void patch(String ipAddress, String url, String token, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer " + token);
        client.patch(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static void delete(String ipAddress, String url, String token, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer " + token);
        client.delete(getAbsoluteUrl(ipAddress, url), params, responseHandler);
    }

    public static String getAbsoluteUrl(String ipAddress, String relativeUrl) {
        return "http://" + ipAddress + ":3443" + relativeUrl;
    }
}
