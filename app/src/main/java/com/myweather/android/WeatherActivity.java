package com.myweather.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.myweather.android.gson.Forecast;
import com.myweather.android.gson.Weather;
import com.myweather.android.util.HttpUtil;
import com.myweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    // 度数
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 代码实行沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    // 活动的布局会显示在状态栏上面
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_weather);

        // 初始化各个控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingImg = findViewById(R.id.bing_img);


        // 从本地缓存（数据库）中读取数据
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);


        // 加载图片
        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null) {
            RequestManager requestManager = Glide.with(this);
            requestManager.load(bingPic).into(bingImg);
        }
        else {
            loadBingImg();
        }
        // 从数据库中取得数据
        String weatherString = preferences.getString("weather", null);
        // 有数据
        if (null != weatherString) {
            // 直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }
        else {
            // 数据库无数据直接查询服务器
            String weatherId = getIntent().getStringExtra("weather_id");
            // 请求数据的时候先将ScrollView进行隐藏，不然空数据的界面看上去会很奇怪。
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

    }


    private void loadBingImg() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {

                ResponseBody body = response.body();
                if (null != body) {
                    final String bingPic = body.string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                            WeatherActivity.this
                    ).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(WeatherActivity.this).load(bingPic).into
                                            (bingImg);
                                }
                            }
                    );
                }
            }
        });
    }

    // 根据天气id请求天气
    private void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                            weatherId + "&key=5b054f4b435046208ef93322a1b79828";

        // http://guolin.tech/api/weather?cityid=CN101010100&key=5b054f4b435046208ef93322a1b79828
        Log.d("TAG", "weatherUrl：" + weatherUrl);

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                       "获取天气信息失败",
                                       Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                System.out.println("响应码是：" + response.code());

                ResponseBody body = response.body();
                if (null != body) {
                    final String responseText = body.string();
                    Log.d("TAG", "onResponse: responseText \r\n" + responseText);
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (null != weather && "200".equals(weather.status)) {
                                        SharedPreferences.Editor editor =
                                                PreferenceManager
                                                        .getDefaultSharedPreferences(
                                                                WeatherActivity.this
                                                        ).edit();
                                        editor.putString("weather", responseText);
                                        editor.apply();
                                        showWeatherInfo(weather);
                                    }
                                    else {
                                        Toast.makeText(WeatherActivity.this,
                                                       "获取天气信息失败",
                                                       Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                }


            }
        });
    }

    // 展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        if (null != weather) {

            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String degree = weather.now.temperature + "℃";
            String weatherInfo = weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            /*
                在循环中动态加载forecast_item.xml布局并设置相应的数据，然后添加到父布局当中。

             */
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(this)
                                          .inflate(
                                                  R.layout.forecast_item_activity_weather,
                                                  forecastLayout, false
                                          );

                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);

                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                minText.setText(forecast.temperature.min);
                forecastLayout.addView(view);
            }
            if (null != weather.aqi) {
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carWash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议：" + weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            // 数据设置完毕，让ScrollView重新变成可见。
            weatherLayout.setVisibility(View.VISIBLE);

        }
    }
}