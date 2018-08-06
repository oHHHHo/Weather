package lyp.com.weather;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import lyp.com.weather.util.City;
import lyp.com.weather.util.CityDB;

/**
 * Created by liyp on 18-8-3.
 */

public class MyApplication extends Application {

    private static Application mApp;
    private List<City> cityList;
    private CityDB mCityDB;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("MyApplication", "Oncreate");
        mApp = this;

        mCityDB = openCityDB();
        initCityList();
    }

    public static Application getInstance() {
        return mApp;
    }

    public CityDB openCityDB() {
        String path = "data" + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + CityDB.CITY_DB_NAME;
        Log.d("file path", path);
        File db = new File(path);
        Log.d("db", path);

        try {
            InputStream is = getAssets().open("city.db");

            FileOutputStream fos = new FileOutputStream(db);
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return new CityDB(this, path);
    }

    private boolean prepareCityList() {
        cityList = mCityDB.getCityList();
/*        for (City city : cityList) {
            String cityName = city.getCity();
            Log.d("CityDB",cityName);
        }*/
        return true;
    }

    private void initCityList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    public List<City> getCityList() {
        return cityList;
    }
}
