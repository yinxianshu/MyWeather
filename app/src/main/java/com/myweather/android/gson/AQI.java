package com.myweather.android.gson;

/*
    空气质量信息
  "aqi": {
    "city": {
      "aqi": "44",
      "pm25": "13"
    }
  }
 */
public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
