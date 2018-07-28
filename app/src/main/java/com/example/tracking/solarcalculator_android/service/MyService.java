package com.example.tracking.solarcalculator_android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;

import com.example.tracking.solarcalculator_android.R;
import com.example.tracking.solarcalculator_android.util.CodeConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MyService extends JobIntentService {

    public MyService() {
        //super(TAG);
    }

    public static void enqueueWork(Context context, Intent work) {

        enqueueWork(context, MyService.class, CodeConstant.JobIntentServiceJobId.MyService, work);

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        loadDataFromPreference();
    }

    private void loadDataFromPreference() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);


        String latitude = sharedPreferences.getString("latitude", "00.00");
        String longitude = sharedPreferences.getString("longitude", "00.00");
        String selectedtimezoneidentifier = sharedPreferences.getString("selectedtimezoneidentifier", TimeZone.getDefault().getID());
        String sunRise = sharedPreferences.getString("sunrise", null);
        String sunSet = sharedPreferences.getString("sunset", null);
        if(sunRise!=null && sunSet!=null){
            pripareNotification(getApplicationContext(),makeMag("Today sun sise time is : ", sunRise, "sun set time is :",sunSet), R.drawable.ic_sun_rise);
        }
    }

    private void pripareNotification(Context context,String msg, int resourseId){
        int iconResourceId = resourseId;
        String title = "Solar Calculation";
        String msg4notification = msg;
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            String notificationId = CodeConstant.AppNotification.id;
            String name = CodeConstant.AppNotification.name;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String desc = CodeConstant.AppNotification.description;

            NotificationChannel channel = new NotificationChannel(notificationId, name, importance);
            channel.setSound(null, null);
            channel.setDescription(desc);
            nm.createNotificationChannel(channel);
            nBuilder = new NotificationCompat.Builder(context,notificationId);


        }else{
            nBuilder = new NotificationCompat.Builder(context);
        }

        nBuilder.setSmallIcon(iconResourceId);
        nBuilder.setContentTitle(title);
        nBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg4notification));
        nBuilder.setContentText(msg4notification);
        nBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        nBuilder.setWhen(System.currentTimeMillis());
        nBuilder.setTicker(title);
        nBuilder.setAutoCancel(true);

        Notification notification = nBuilder.build();
        nm.notify(500,notification);

    }

    public String makeMag(String sunRiseMsg, String sunRiseTime, String sunSetMsg, String sunSetTime) {

        StringBuilder sb = new StringBuilder();
        sb.append(sunRiseMsg);
        sb.append(" ");
        sb.append(formateTime(sunRiseTime));
        sb.append(" and ");
        sb.append(sunSetMsg);
        sb.append(" ");
        sb.append(formateTime(sunSetTime));

        return sb.toString();

    }

    private String formateTime(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
        Date dt;
        try {
            dt = sdf.parse(time);
            return sdfs.format(dt);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
