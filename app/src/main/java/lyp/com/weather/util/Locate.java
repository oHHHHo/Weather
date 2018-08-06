package lyp.com.weather.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import lyp.com.weather.MyApplication;
import lyp.com.weather.R;
import lyp.com.weather.SelectCity;

/**
 * Created by liyp on 18-8-6.
 */

public class Locate extends Activity {

    private LocationClient locationClient;
    private MyLocationListener locationListener;
    private TextView info;
    private Button ok;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean  isFirstLocate = true;

    private MyApplication myApplication;
    private List<City> mCityList;
    private String cityName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.locate);

        // permission
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Locate.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Locate.this, permissions, 1);
        }

        info = findViewById(R.id.locate_info);
        ok = findViewById(R.id.ok);
        mapView = findViewById(R.id.map_bd);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        requestLocation();
        mapView.setVisibility(View.GONE);

        myApplication = (MyApplication) MyApplication.getInstance();
        mCityList = myApplication.getCityList();

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setVisibility(View.VISIBLE);
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int locateCityCode = 0;
                for (int i = 0; i < mCityList.size(); i++) {
                    String city = mCityList.get(i).getCity();
                    if (cityName != null && cityName.contains(city)) {
                        String number = mCityList.get(i).getNumber();
                        locateCityCode = Integer.parseInt(number);
                        Log.e("SSSS",locateCityCode+"");
                        break;
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("result", locateCityCode);
                Locate.this.setResult(RESULT_OK, intent);

                //用SharePreference 存储最近一次的citycode
                SharedPreferences sp = getSharedPreferences("cityCodePreference", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("cityCode", locateCityCode);
                editor.commit();
                finish();
            }
        });

    }

    private void requestLocation() {
        locationClient = new LocationClient(this);
        locationListener = new MyLocationListener();
        locationClient.registerLocationListener(locationListener);
        initLocation();
        locationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd0911");
        option.setScanSpan(5000);
        locationClient.setLocOption(option);
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder  currentPosition =  new StringBuilder();
            currentPosition.append("维度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");

            Log.e("tag","当前的定位方式="+bdLocation.getLocType());
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络");
            }
            info.setText(currentPosition);
            cityName = bdLocation.getCity();
            ok.setText(cityName);


            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation ||bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){

                if(isFirstLocate){
                    LatLng  ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    isFirstLocate = false;
                }
                MyLocationData.Builder  locationBuilder = new MyLocationData.Builder();
                locationBuilder.latitude(bdLocation.getLatitude());
                locationBuilder.longitude(bdLocation.getLongitude());
                MyLocationData locationData = locationBuilder.build();
                baiduMap.setMyLocationData(locationData);

            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        switch(requestCode) {
            //requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 1:
                BAIDU_READ_PHONE_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取到权限，做相应处理
                    //调用定位SDK应确保相关权限均被授权，否则会引起定位失败
                } else{
                    //没有获取到权限，做特殊处理
                }
                break;
            default:
                break;
        }
    }
}
