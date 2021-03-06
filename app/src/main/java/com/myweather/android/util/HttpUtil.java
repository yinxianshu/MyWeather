package com.myweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;


// 使用OkHttp3收发网络请求
public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        /*
            安排请求在某个时间点执行，通常立即运行，除非当前有其他几个请求执行。
            此次连接稍后将回得到响应或者失败的异常回调。
         */

        client.newCall(request).enqueue(callback);
    }
}
