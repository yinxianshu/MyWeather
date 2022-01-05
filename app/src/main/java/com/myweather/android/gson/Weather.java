package com.myweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/*
    总的实例类来引用basic 、aqi 、now 、suggestion这4个类。
 */
public class Weather {

    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    /*
        由于daily_forecast 中包含的是一个数组，
        因此这里使用了List集合来引用Forecast类。
     */
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
