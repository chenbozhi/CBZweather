package com.CBZweather.android.util;


import okhttp3.OkHttpClient;
import okhttp3.Request;

//通过address发送网络请求，返回到callback

public class HttpUtil {

    public static void sendOKHttpRequest(String address, okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
