package cn.cccxu.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import com.alibaba.fastjson.JSONObject;

public class MainActivity extends AppCompatActivity {

    // 纬度
    public double latitude = 0.0;
    // 经度
    public double longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get location
        initLocation(this);
        if(!isNetworkConnected(this)){
            Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
            return;
        }
        new DownloadUpdate().execute();
        Toast.makeText(this, "INFORMATION UPDATED", Toast.LENGTH_SHORT).show();
    }

    public void btnClick(View view) {
        if(!isNetworkConnected(this)){
            Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
            return;
        }
        //get and set date
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String date = Integer.toString(day) + "/" + Integer.toString(month + 1)+ "/" + Integer.toString(year);
        ((TextView)findViewById(R.id.tv_date)).setText(date);
        //initLocation(this);
        new DownloadUpdate().execute();
        Toast.makeText(this, "INFORMATION UPDATED", Toast.LENGTH_SHORT).show();

    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            //检查并申请权限
            int i = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
            if(i == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1000);
            }

            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return true;
            }
        }
        return false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //NOTICE:
    //THIS FUNCTION IS FROM  Sean_帅恩 AT https://blog.csdn.net/yy1300326388/article/details/47313701
    //AND I ADDED CHECK AND REQUEST PERMISSION FOR IT
    //BUT DUE TO SOME REASON I FINALY USED DR.FENO'S JSON FILE FROM https://mpianatra.com/Courses/forecast.json
    //SO THE LOCATION INFO IS NEVER USED
    //BUT A WEATHER APP NEED TO HAS THE ABILITY TO GET LOCATION INFO SO IT STILL HERE
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
     /**
     * 初始化位置信息
     *
     * @param context
     */
    public void initLocation(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        //检查并申请权限
        int i = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if(i == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } else {
            LocationListener locationListener = new LocationListener() {

                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {

                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {

                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {

                }

                // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {

                    }
                }
            };
            locationManager
                    .requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000, 0, locationListener);
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude(); // 经度
                longitude = location.getLongitude(); // 纬度
            }
        }
    }

    private class DownloadUpdate extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {

            //存放地点与天气信息
            //0: city code
            //1: city name
            //2: today weather
            //3: today temperature
            //4: tomorrow weather
            //5-7: so on
            String[] info = new String[8];

            try {
                //request URL
                String urlStr = "https://mpianatra.com/Courses/forecast.json";
                HttpsURLConnection conn = null;
                BufferedReader reader;

                URL url = new URL(urlStr);

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine())!=null){
                    buffer.append(line + "\n");
                }

                String json = buffer.toString();

                JSONObject jsonObj = JSONObject.parseObject(json);

                JSONObject cityInfo = jsonObj
                        .getJSONObject("city");
                info[0] = cityInfo.getString("name");
                JSONObject temInfo = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(1)
                        .getJSONObject("main");
                int tempC = (int)(Double.parseDouble(temInfo.getString("temp")) - 273.15);
                info[1] = String.valueOf(tempC);

                //get weather condition
                JSONObject weatherInfo = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(0)
                        .getJSONArray("weather")
                        .getJSONObject(0);
                info[2] = weatherInfo.getString("id");
                JSONObject weatherInfo2 = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(2)
                        .getJSONArray("weather")
                        .getJSONObject(0);
                info[3] = weatherInfo2.getString("id");
                JSONObject weatherInfo3 = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(10)
                        .getJSONArray("weather")
                        .getJSONObject(0);
                info[4] = weatherInfo3.getString("id");
                JSONObject weatherInfo4 = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(18)
                        .getJSONArray("weather")
                        .getJSONObject(0);
                info[5] = weatherInfo4.getString("id");
                JSONObject weatherInfo5 = jsonObj
                        .getJSONArray("list")
                        .getJSONObject(26)
                        .getJSONArray("weather")
                        .getJSONObject(0);
                info[6] =weatherInfo5.getString("id");

                for(int i = 2; i <= 6; i++){
                    if(info[i] == null){
                        return null;
                    }
                    int codeN = Integer.valueOf(info[i]);
                    int code;
                    if(codeN == 800){
                        code = R.drawable.sunny_small;
                    } else if((codeN >= 600 && codeN < 700) || (codeN < 600 && codeN >= 500)){
                        code =  R.drawable.rainy_small;
                    } else if(codeN >= 200 && codeN < 300){
                        code = R.drawable.windy_small;
                    } else {
                        code = R.drawable.partly_sunny_small;
                    }
                    info[i] = String.valueOf(code);
                }

                return info;
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] info) {
            if(info == null){
                return;
            }
            //Update the location name
            ((TextView)findViewById(R.id.tv_location)).setText(info[0]);
            ((TextView)findViewById(R.id.temperature_of_the_day)).setText(info[1]);
            //today
            ((ImageView)findViewById(R.id.img_weather_condition)).setImageResource(Integer.valueOf(info[2]));
            ((ImageView)findViewById(R.id.img_weather_condition1)).setImageResource(Integer.valueOf(info[3]));
            ((ImageView)findViewById(R.id.img_weather_condition2)).setImageResource(Integer.valueOf(info[4]));
            ((ImageView)findViewById(R.id.img_weather_condition3)).setImageResource(Integer.valueOf(info[5]));
        }
    }
}

