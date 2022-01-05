package com.myweather.android.gson;

import com.google.gson.annotations.SerializedName;

/*
daily_forecast包含的是数组，数组中的每一项都代表着未来一天的天气信息。
"daily_forecast": [
    {
      "date": "2016-08-08",
      "cond": {
        "txt_d": "阵雨"
      },
      "tmp": {
        "max": "34",
        "min": "27"
      }
    },
    {
      "date": "2016-08-09",
      "cond": {
        "txt_d": "多云"
      },
      "tmp": {
        "max": "35",
        "min": "29"
      }
    },
    ...
 */
public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
