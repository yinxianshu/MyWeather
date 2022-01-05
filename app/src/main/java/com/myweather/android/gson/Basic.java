package com.myweather.android.gson;

import com.google.gson.annotations.SerializedName;

/*
    "basic":{
        "city":"苏州",
        "id":"CN101190401",
        "update":{
        "loc":"2016-08-08 21:58"
        }
    }
 */
public class Basic {

    /**
     * 由于JSON中的一些字段可能不太适合直接作为Java字段来命名，因此这里使用了
     * 注解的方式来让JSON字段和Java字段之间建立映射关系。
     */
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }

}
