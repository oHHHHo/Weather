package lyp.com.weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import lyp.com.weather.util.City;

/**
 * Created by liyp on 18-8-3.
 */

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView backBtn;
    private ListView cityListLv;
    private EditText searchEt;
    private Button searchBt;
    private boolean SARCH_FLAG;
    private int checkedCityCode;

    private List<City> mCityList;
    private MyApplication myApplication;
    private ArrayList<String> cityInfo, searchList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        backBtn = findViewById(R.id.title_selectCity_back);
        searchEt = findViewById(R.id.selectcity_search);
        searchBt = findViewById(R.id.selectcity_search_btn);
        backBtn.setOnClickListener(this);
        searchBt.setOnClickListener(this);

        myApplication = (MyApplication) MyApplication.getInstance();
        mCityList = myApplication.getCityList();
        cityInfo = new ArrayList<>();
        for (int i = 0; i < mCityList.size(); i++) {
            String no_ = Integer.toString(i+1);
            String number = mCityList.get(i).getNumber();
            String province = mCityList.get(i).getProvince();
            String cityName = mCityList.get(i).getCity();
            cityInfo.add("NO." + no_ + ":" + number + "-" + province + "_" + cityName);
        }

        cityListLv = findViewById(R.id.selectcity_lv);
        adapter = new ArrayAdapter<String>(SelectCity.this,
                android.R.layout.simple_list_item_1, cityInfo);
        cityListLv.setAdapter(adapter);

        //添加ListView点击事件
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int updateCityCode = Integer.parseInt(mCityList.get(position).getNumber());
                Log.d("update city code", Integer.toString(updateCityCode));
                int code;
                if (SARCH_FLAG) {
                    code = checkedCityCode;
                } else {
                    code = updateCityCode;
                }
                Intent intent = new Intent();
                intent.putExtra("result", code);
                SelectCity.this.setResult(RESULT_OK, intent);

                //用SharePreference 存储最近一次的citycode
                SharedPreferences sp = getSharedPreferences("cityCodePreference", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("cityCode", code);
                editor.commit();

                checkedCityCode = 0;
                finish();
            }
        };
        cityListLv.setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_selectCity_back:
                finish();
                break;
            case R.id.selectcity_search_btn:
                SARCH_FLAG = true;
                String city = searchEt.getText().toString();
                Log.d("Search", city);
                searchList = new ArrayList<>();
                for (int i = 0; i < mCityList.size(); i++) {
                    String cityName = mCityList.get(i).getCity();
                    if (city != null && cityName.equals(city)) {
                        String no_ = Integer.toString(i+1);
                        String number = mCityList.get(i).getNumber();
                        String province = mCityList.get(i).getProvince();
                        searchList.add("NO." + no_ + ":" + number + "-" + province + "_" + cityName);
                        checkedCityCode = Integer.parseInt(number);
                        break;
                    }
                }
                adapter = new ArrayAdapter<String>(SelectCity.this,
                        android.R.layout.simple_list_item_1, searchList);
                cityListLv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            default:
                break;
        }
    }
}
