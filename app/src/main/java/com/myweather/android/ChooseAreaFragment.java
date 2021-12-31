package com.myweather.android;


import android.app.ProgressDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.myweather.android.db.City;
import com.myweather.android.db.County;
import com.myweather.android.db.Province;
import com.myweather.android.util.HttpUtil;
import com.myweather.android.util.Utility;

import org.litepal.LitePal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * 遍历省市县数据的碎片（Fragment）
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    // 初始化控件
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,
                                     container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_list_item_1,
                dataList
        );
        // 把ArrayAdapter设为listView的数据适配器。
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 给listView设置监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 当点击listView子项的时候
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // 当前选中项和LEVEL_PROVINCE（0）相等时
                if (currentLevel == LEVEL_PROVINCE) {
                    // 得到选中的省的位置和值
                    selectedProvince = provinceList.get(position);
                    // 查询市
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    // 查询县
                    queryCounties();
                }
            }
        });
        // 点击省以后出现了后退箭头，为这个后退箭头设置监听器
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前选中项和LEVEL_COUNTY（2）相等时
                if (currentLevel == LEVEL_COUNTY) {
                    // 查询所属的上一级市
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY) {
                    // 查询所属的上一级省
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        /*
          将头布局的标题设置成中国，将返回按钮隐藏起来，
          因为省级列表已经不能再返回了。
         */
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        // 从数据库中得到数据
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 查询省会把当前级别设为省的级别LEVEL_PROVINCE
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            // 没有数据，从服务器中获取。
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where(
                "provinceId = ?",
                String.valueOf(selectedProvince.getId())
        ).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where(
                "cityId = ?",
                String.valueOf(selectedCity.getId())
        ).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 查询县会把当前级别设为县的级别LEVEL_COUNTY
            currentLevel = LEVEL_COUNTY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" +
                             cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(
                    @NonNull Call call,
                    @NonNull Response response) throws IOException {

                String responseText =
                        Objects.requireNonNull(response.body())
                               .string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(
                            responseText, selectedProvince.getId()
                    );
                }
                else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(
                            responseText, selectedCity.getId()
                    );
                }
                if (result) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {

                // 通过runOnUiThread()方法回到主线程处理逻辑
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        e.printStackTrace();
                        Toast.makeText(
                                getContext(), "加载失败", Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*
     Fragment中无法使用onClick绑定事件
     public void back(View view) {
     }
    */
}
