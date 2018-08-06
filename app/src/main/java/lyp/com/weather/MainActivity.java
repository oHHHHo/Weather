package lyp.com.weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import lyp.com.weather.util.Locate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //title
    private ImageView updateBtn;
    private ImageView selectCityBtn;
    private ImageView locateBtn;

    //todayweather
    private TextView cityNameT, cityT, timeT, humidityT, weekT, pmDataT, pmQualityT, temperatureT,
            climateT, windT;
    private ImageView weatherImg, pmStateImg;
    //future
    private TextView week1T, temperature1T, climate1T, wind1T, week2T, temperature2T, climate2T, wind2T,
            week3T, temperature3T, climate3T, wind3T;

    private TodayWeather todayWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d("MainActivity", "Oncreate");

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        updateBtn = findViewById(R.id.title_city_update);
        updateBtn.setOnClickListener(this);
        selectCityBtn = findViewById(R.id.title_city_manager);
        selectCityBtn.setOnClickListener(this);
        locateBtn = findViewById(R.id.title_city_locate);
        locateBtn.setOnClickListener(this);

        initView();

        //检查网络链接状态
        if(CheckNet.getNetState(this) == CheckNet.NET_NONE) {
            Log.d("WEATHER","网络不通");
            Toast.makeText(this, "网络不通", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("WEATHER","网络OK");
            Toast.makeText(this, "网络OK", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_city_update) {
            SharedPreferences sp = getSharedPreferences("cityCodePreference", Activity.MODE_PRIVATE);
            int code = sp.getInt("cityCode", 0);
            if (code != 0) {
                getWeatherDatafromNet(code+"");
            } else {
                getWeatherDatafromNet("101010100");
            }
        }
        if (v.getId() == R.id.title_city_manager) {
            Intent intent = new Intent(this, SelectCity.class);
            startActivityForResult(intent, 1234);
        }
        if (v.getId() == R.id.title_city_locate) {
            Intent intent = new Intent(this, Locate.class);
            startActivityForResult(intent, 2345);
        }
    }

    private void initView() {

        //title
        cityNameT = findViewById(R.id.title_city_name);

        //today weather
        cityT = findViewById(R.id.todayinfo1_cityName);
        timeT = findViewById(R.id.todayinfo1_updateTime);
        humidityT = findViewById(R.id.todayinfo1_humidity);
        weekT = findViewById(R.id.todayinfo1_week);
        pmDataT = findViewById(R.id.todayinfo1_pm25);
        pmQualityT = findViewById(R.id.todayinfo1_pm25status);
        temperatureT = findViewById(R.id.todayinfo1_temperature);
        climateT = findViewById(R.id.todayinfo1_weatherState);
        windT = findViewById(R.id.todayinfo1_wind);

        weatherImg = findViewById(R.id.todayinfo1_weatherStatusImg);
        pmStateImg = findViewById(R.id.todayinfo1_pm25img);

        cityNameT.setText("N/A");
        cityT.setText("N/A");
        timeT.setText("N/A");
        humidityT.setText("N/A");
        weekT.setText("N/A");
        pmDataT.setText("N/A");
        pmQualityT.setText("N/A");
        temperatureT.setText("N/A");
        climateT.setText("N/A");
        windT.setText("N/A");

        //future
        week1T = findViewById(R.id.future1_no1_week);
        week2T = findViewById(R.id.future2_no1_week);
        week3T = findViewById(R.id.future3_no1_week);
        temperature1T = findViewById(R.id.future1_no1_temperature);
        temperature2T = findViewById(R.id.future2_no1_temperature);
        temperature3T = findViewById(R.id.future3_no1_temperature);
        climate1T = findViewById(R.id.future1_no1_weatherstate);
        climate2T = findViewById(R.id.future2_no1_weatherstate);
        climate3T = findViewById(R.id.future3_no1_weatherstate);
        wind1T = findViewById(R.id.future1_no1_wind);
        wind2T = findViewById(R.id.future2_no1_wind);
        wind3T = findViewById(R.id.future3_no1_wind);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void getWeatherDatafromNet(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("Address:", address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(address);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuffer sb = new StringBuffer();
                    String str;
                    while ((str = reader.readLine()) != null ) {
                        sb.append(str);
                        Log.d("date from url",str);
                    }
                    String response = sb.toString();
//                    Log.d("response",response);
                    todayWeather = parseXML(response);
                    if (todayWeather != null) {
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather todayWeather = null;

        int fengliCount = 0;
        int fengxiangCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();
            Log.d("WEATHER","start parse xml");

            while(eventType!=xmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                    //文档开始位置
                    case XmlPullParser.START_DOCUMENT:
                        Log.d("parse","start doc");
                        break;
                    //标签元素开始位置
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp"))
                        {
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather!=null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                Log.d("city", xmlPullParser.getText());
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                Log.d("updatetime", xmlPullParser.getText());
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                Log.d("wendu", xmlPullParser.getText());
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("fengli", xmlPullParser.getText());
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                Log.d("shidu", xmlPullParser.getText());
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("fengxiang", xmlPullParser.getText());
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                Log.d("pm25", xmlPullParser.getText());
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                Log.d("quelity", xmlPullParser.getText());
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("date", xmlPullParser.getText());
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("high", xmlPullParser.getText());
                                todayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("low", xmlPullParser.getText());
                                todayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                Log.d("type", xmlPullParser.getText());
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                Log.d("date", xmlPullParser.getText());
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                Log.d("high", xmlPullParser.getText());
                                todayWeather.setHigh1(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                Log.d("low", xmlPullParser.getText());
                                todayWeather.setLow1(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                Log.d("type", xmlPullParser.getText());
                                todayWeather.setType1(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                Log.d("fengli", xmlPullParser.getText());
                                todayWeather.setFengli1(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                Log.d("date", xmlPullParser.getText());
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                Log.d("high", xmlPullParser.getText());
                                todayWeather.setHigh2(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                Log.d("low", xmlPullParser.getText());
                                todayWeather.setLow2(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                Log.d("type", xmlPullParser.getText());
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                Log.d("fengli", xmlPullParser.getText());
                                todayWeather.setFengli2(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                Log.d("date", xmlPullParser.getText());
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                Log.d("high", xmlPullParser.getText());
                                todayWeather.setHigh3(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                Log.d("low", xmlPullParser.getText());
                                todayWeather.setLow3(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                Log.d("type", xmlPullParser.getText());
                                todayWeather.setType3(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                Log.d("fengli", xmlPullParser.getText());
                                todayWeather.setFengli3(xmlPullParser.getText());
                                fengliCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return todayWeather;
    }

    private void updateTodayWeather(TodayWeather todayWeather) {

        cityNameT.setText(todayWeather.getCity()+"天气");
        cityT.setText(todayWeather.getCity());
        timeT.setText(todayWeather.getUpdatetime());
        humidityT.setText("湿度:"+todayWeather.getShidu());
        pmDataT.setText(todayWeather.getPm25());
        pmQualityT.setText(todayWeather.getQuality());
        weekT.setText(todayWeather.getDate());
        temperatureT.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateT.setText(todayWeather.getType());
        windT.setText("风力:"+todayWeather.getFengli());
        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();

        if (todayWeather.getPm25() != null) {
            int pm25 = Integer.parseInt(todayWeather.getPm25());
            if (pm25 <= 50) {
                pmStateImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25 >= 51 && pm25 <= 100) {
                pmStateImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25 >= 101 && pm25 <= 150) {
                pmStateImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25 >= 151 && pm25 <= 200) {
                pmStateImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25 >= 201 && pm25 <= 300) {
                pmStateImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            }
        }
        if (todayWeather.getType() != null) {
            Log.d("type", todayWeather.getType());
            switch (todayWeather.getType()) {
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }

        week1T.setText(todayWeather.getDate1());
        week2T.setText(todayWeather.getDate2());
        week3T.setText(todayWeather.getDate3());
        temperature1T.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        temperature2T.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        temperature3T.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        climate1T.setText(todayWeather.getType1());
        climate2T.setText(todayWeather.getType2());
        climate3T.setText(todayWeather.getType3());
        wind1T.setText("风力:" + todayWeather.getFengli1());
        wind2T.setText("风力:" + todayWeather.getFengli2());
        wind3T.setText("风力:" + todayWeather.getFengli3());

        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1234:
                case 2345:
                    String cityCode = data.getExtras().getInt("result") + "";
                    getWeatherDatafromNet(cityCode);
                    break;
                default:
                    break;


            }
        }
    }
}
