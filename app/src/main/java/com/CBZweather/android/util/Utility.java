package com.CBZweather.android.util;

import android.text.TextUtils;

import com.CBZweather.android.db.City;
import com.CBZweather.android.db.County;
import com.CBZweather.android.db.Province;

import org.json.JSONArray;
import org.json.JSONObject;

//这里的方法是解析json数据的，并会保存到数据库中
public class Utility {

    //解析处理省级数据,会
    // //将解析的数据保存在数据库中

    //设置了provinceCode，
    // 和name
    public static boolean handleProvinceResponse(String response)
    {
        if(!TextUtils.isEmpty(response))
        {
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0;i < allProvince.length();i++){
                    JSONObject provinceJSONObject = allProvince.getJSONObject(i);

                    Province province = new Province();
                    province.setProvinceName(provinceJSONObject.getString("name"));
                    province.setProvinceCode(provinceJSONObject.getInt("id"));
                    province.save();  //保存在数据库
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析处理市级数据

    // //将解析的数据保存在数据库中

    //用到的字段有 cityCode， name 和provinceId

    public static boolean handleCityResponse(String response, int provinceId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try {
                JSONArray allCitis = new JSONArray(response);
                for (int i = 0;i < allCitis.length();i++){
                    JSONObject cityJSONObject = allCitis.getJSONObject(i);

                    City city = new City();
                    city.setCityName(cityJSONObject.getString("name"));
                    city.setCityCode(cityJSONObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析处理县级数据

    // //将解析的数据保存在数据库中

    //用到了name， weatherid 和cityId
    public static boolean handleCountyResponse(String response, int cityId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try {
                JSONArray allcounties = new JSONArray(response);
                for (int i = 0;i < allcounties.length();i++){
                    JSONObject countyJSONObject = allcounties.getJSONObject(i);

                    County county = new County();
                    county.setCountyName(countyJSONObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(countyJSONObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
