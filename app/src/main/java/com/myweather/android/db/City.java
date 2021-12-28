package com.myweather.android.db;

import org.litepal.crud.LitePalSupport;

// 实体类 - 市
public class City extends LitePalSupport {

    private int id;
    private String cityName;
    private int cityCode;
    // 当前市所属省的id值
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }


}
