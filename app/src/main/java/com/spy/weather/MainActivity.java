package com.spy.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    Button start,stop,play;
    private MediaRecorder rec;
    final Handler handler = new Handler();
    Timer timer = new Timer();
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String url = "http://faldukan.com/weather/saveAudio.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start =findViewById(R.id.start);
        stop =findViewById(R.id.stop);
        play =findViewById(R.id.play);




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckPermissions()) {





                    TimerTask doAsynchronousTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {

                                        record();
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        }
                    };
                    timer.schedule(doAsynchronousTask, 0, 60000);






                }else{
                    RequestPermissions();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    stop();

            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String file_path=getApplicationContext().getFilesDir().getPath();

                File file= new File(file_path);
                MediaPlayer mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(file+"/sunil2.mp3");
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();

            }
        });

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

        String file_name=file+"/sunil2"+".mp3";
        rec.setOutputFile(file_name);

        try {
            rec.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Sorry! file creation failed!"+e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }
        rec.start();
        Toast.makeText(MainActivity.this,"Record started",Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                sendAudio(file_name);

            }
        }, 60000);

    }

    public void stop(){
        rec.stop();
        rec.reset();
    }



    private void sendAudio(String selectedPath) {
        try{



            File file=new File(selectedPath);

            RequestParams params = new RequestParams();
            params.put("audio",file);
            Log.i("Api called :",file.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(url, params,new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                    String s=new String(responseBody);
                    Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    Log.i("Api called :",error.toString());

                }
            });

        }
        catch (Exception e)
        {

        }
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
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.RECORD_AUDIO,  Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}