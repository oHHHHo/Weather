package lyp.com.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //检查网络链接状态
        if(CheckNet.getNetState(this) == CheckNet.NET_NONE) {
            Log.d("WEATHER","网络不通");
            Toast.makeText(this, "网络不通", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("WEATHER","网络OK");
            Toast.makeText(this, "网络OK", Toast.LENGTH_SHORT).show();
        }

        getWeatherDatafromNet("101010100");

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
                    Log.d("response",response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
