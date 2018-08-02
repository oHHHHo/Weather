package lyp.com.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        updateBtn = findViewById(R.id.title_city_update);
        updateBtn.setOnClickListener(this);

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
        if(v.getId() == R.id.title_city_update) {
            getWeatherDatafromNet("101010100");
        }
    }

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
                    parseXml(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseXml(String xmlData) {
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

            while (eventType != xmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //文档开始位置
                    case XmlPullParser.START_DOCUMENT:
                        Log.d("parse", "start doc");
                        break;
                    //标签元素开始位置
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("city")) {
                            eventType = xmlPullParser.next();
                            Log.d("city",xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime" + "")) {
                            eventType = xmlPullParser.next();
                            Log.d("updatetime",xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            Log.d("wendu", xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("fengli") && fengliCount==0 ) {
                            eventType = xmlPullParser.next();
                            Log.d("fengli",xmlPullParser.getText());
                            fengliCount++;
                        } else if (xmlPullParser.getName().equals("shidu")) {
                            eventType = xmlPullParser.next();
                            Log.d("shidu",xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount== 0) {
                            eventType = xmlPullParser.next();
                            Log.d("fengxiang",xmlPullParser.getText());
                            fengxiangCount++;
                        } else if (xmlPullParser.getName().equals("pm25")) {
                            eventType=xmlPullParser.next();
                            Log.d("pm25",xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("quality")) {
                            eventType = xmlPullParser.next();
                            Log.d("quelity",xmlPullParser.getText());
                        }else if (xmlPullParser.getName().equals("date") && dateCount==0 ) {
                            eventType = xmlPullParser.next();
                            Log.d("date",xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount==0 ) {
                            eventType = xmlPullParser.next();
                            Log.d("high",xmlPullParser.getText());
                            highCount++;
                        } else if(xmlPullParser.getName().equals("low") && lowCount==0 ) {
                            eventType = xmlPullParser.next();
                            Log.d("low",xmlPullParser.getText());
                            lowCount++;
                        } else if(xmlPullParser.getName().equals("type") && typeCount==0 ) {
                            eventType = xmlPullParser.next();
                            Log.d("type",xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
