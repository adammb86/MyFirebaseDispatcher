package com.example.adammb.myfirebasedispatcher;


import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class MyJobService extends JobService {
    public static final String TAG=MyJobService.class.getSimpleName();
    final String APP_ID="d274b12a257d175910a465321e47dd8d";
    public static String EXTRAS_CITY="extra_city";

    private void getCurrentWeather(final JobParameters job){
        String city=job.getExtras().getString(EXTRAS_CITY);

        AsyncHttpClient client=new AsyncHttpClient();
        String url="http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+APP_ID;

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result=new String(responseBody);
                Log.d(TAG,result);

                try {
                    JSONObject responseObject=new JSONObject(result);
                    String currentWeather=responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description=responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin=responseObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius=tempInKelvin-273;
                    String temperature=new DecimalFormat("##.##").format(tempInCelcius);
                    String title="Current Weather";
                    String message=currentWeather+", "+description+" with "+temperature+" celcius ";
                    int notifId=100;

                    showNotification(getApplicationContext(),title,message,notifId);
                    jobFinished(job,false);

                } catch (JSONException e) {
                    jobFinished(job,true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jobFinished(job,true);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat=(NotificationManager)context.getSystemService((Context.NOTIFICATION_SERVICE));
        Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,"")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_30_black_24dp)
                .setContentTitle(message)
                .setColor(ContextCompat.getColor(context,android.R.color.black))
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(alarmSound);

        notificationManagerCompat.notify(notifId,builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("TEST", "TEST Executed");
        getCurrentWeather(job);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        getCurrentWeather(job);

        return true;
    }
}
