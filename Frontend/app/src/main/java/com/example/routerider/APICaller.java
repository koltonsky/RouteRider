package com.example.routerider;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APICaller {
    String hostUrl = "https://10.0.2.2:8081/";

    public interface ApiCallback {
        void onResponse(String responseBody);
        void onError(String errorMessage);
    }

    public void APICall(String url, String requestBody, HttpMethod method, ApiCallback callback) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = null;
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            body = RequestBody.create(JSON, requestBody);
        }

        Request.Builder requestBuilder = new Request.Builder().url(hostUrl + url);
        switch (method) {
            case GET:
                requestBuilder.get();
                break;
            case POST:
                requestBuilder.post(body);
                break;
            case PUT:
                requestBuilder.put(body);
                break;
            case DELETE:
                requestBuilder.delete();
                break;
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } catch (IOException e) {
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
        });
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }
}
