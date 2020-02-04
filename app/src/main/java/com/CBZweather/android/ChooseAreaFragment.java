package com.CBZweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.CBZweather.android.db.City;
import com.CBZweather.android.db.County;
import com.CBZweather.android.db.Province;
import com.CBZweather.android.util.HttpUtil;
import com.CBZweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.CBZweather.android.util.HttpUtil.sendOKHttpRequest;

public class ChooseAreaFragment extends Fragment {

    private ProgressDialog progressDialog;

    private TextView titleText;   //显示上一级名称

    private Button backButton;  //返回按钮

    private ListView listView;  //显示省市县数据

    private ArrayAdapter<String> arrayAdapter;   //适配器，由于只需要显示名字，泛型用String就好了

    private List<String> dataList = new ArrayList<>();   //用于保存省市县数据


    private List<Province> provinceList; //省列表
    private List<City> cityList;      //市列表
    private List<County> countyList;  //县列表

    private Province selectedProvince = new Province();   //当点击省列表某个省后，保存这个省

    private City selectedCity = new City() ;   //当点击市列表某个市后，保存这个市



    public static final int LEAVE_PROVINCE = 0;  //这三个常量分别表示省级，市级，县级
    public static final int LEAVE_CITY = 1;
    public static final int LEAVE_COUNTY = 2;

    private int currentLeavel;   //表示当前处于哪个等级， 有上面三个常量可选

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_textView);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();  //填充省级数据

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLeavel == LEAVE_PROVINCE)
                {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }
                else if(currentLeavel == LEAVE_CITY)
                {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
                else if(currentLeavel == LEAVE_COUNTY)
                {
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    //getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLeavel == LEAVE_COUNTY)
                {
                    queryCities();
                }
                else if(currentLeavel == LEAVE_CITY)
                {
                    queryProvinces();
                }
            }
        });
    }

    //查询省
    private void queryProvinces()
    {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);

        provinceList = DataSupport.findAll(Province.class); //从数据库中查询

        if(provinceList.size() > 0) //如果数据库中有
        {
            dataList.clear();
            for(Province province :provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeavel = LEAVE_PROVINCE;
        }
        else {
            String address = "http://guolin.tech/api/china";  //如果数据库中没有，则从服务器上查
            queryFromServer(address, "province");
        }

    }

    //查询市
    private void queryCities()
    {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0)
        {
            dataList.clear();
            for(City city :cityList)
            {
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeavel = LEAVE_CITY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    //查询县
    private void queryCounties()
    {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0)
        {
            dataList.clear();
            for(County county: countyList)
            {
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeavel = LEAVE_COUNTY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    //根据地址和类型查询数据
    private void queryFromServer(String address, final String type)
    {
        showProgressDialog();
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();  //json格式的数据
                boolean result = false;
                if("province".equals(type))
                {
                    result = Utility.handleProvinceResponse(responseText);  //保存了省数据
                }
                else if("city".equals(type))
                {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }
                else if("county".equals(type))
                {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {  //更新UI
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type))
                            {
                                queryProvinces();
                            }
                            else if("city".equals(type))
                            {
                                queryCities();
                            }
                            else if("county".equals(type))
                            {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    //显示进度条
    private void showProgressDialog()
    {
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度条
    private void closeProgressDialog()
    {
        if(progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }
}
