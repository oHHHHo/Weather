package lyp.com.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import lyp.com.weather.util.City;
import lyp.com.weather.util.CityDB;

/**
 * Created by liyp on 18-8-3.
 */

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView backBtn;
    private ListView cityListLv;
    private List<City> mCityList;
    private MyApplication myApplication;
    private ArrayList<String> cityNameList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        backBtn = findViewById(R.id.title_selectCity_back);
        backBtn.setOnClickListener(this);

        myApplication = (MyApplication) MyApplication.getInstance();
        mCityList = myApplication.getCityList();
        cityNameList = new ArrayList<>();
        for (int i = 0; i < mCityList.size(); i++) {
            String cityName = mCityList.get(i).getCity();
            cityNameList.add(cityName);
        }

        cityListLv = findViewById(R.id.selectcity_lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,
                android.R.layout.simple_list_item_1, cityNameList);
        cityListLv.setAdapter(adapter);

        //添加ListView点击事件
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int updateCityCode = Integer.parseInt(mCityList.get(position).getNumber());
                Log.d("update city code", Integer.toString(updateCityCode));
                Intent intent = new Intent();
                intent.putExtra("result", updateCityCode);
                SelectCity.this.setResult(RESULT_OK, intent);
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
            default:
                break;
        }
    }
}
