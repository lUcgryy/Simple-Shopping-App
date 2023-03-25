package com.example.simple.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simple.activity.MainActivity;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Login {
    public static void refreshToken(SharedPreferences sp, AppCompatActivity activity) {
        String refreshToken = sp.getString("refreshToken","");
        String address = sp.getString("ipAddress", "");
        if (!refreshToken.isEmpty()) {
            HttpUtils.get(address, "/api/users/token", refreshToken, null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", response.toString());
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        String accessToken = json.getString("accessToken");
                        sp.edit().putString("accessToken", accessToken).apply();
                        activity.finish();
                        activity.startActivity(activity.getIntent());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("error", errorResponse.toString());
                    sp.edit().putString("accessToken","").apply();
                    try {
                        JSONObject json = new JSONObject(errorResponse.toString());
                        String message = json.getString("message");
                        if (message.compareTo("jwt expired") == 0) {
                            Intent intent = new Intent(activity.getBaseContext(), MainActivity.class);
                            activity.startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
