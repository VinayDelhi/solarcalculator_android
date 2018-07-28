package com.example.tracking.solarcalculator_android.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.tracking.solarcalculator_android.service.MyService;

public class MyBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myService = new Intent(context, MyService.class);
        myService.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            MyService.enqueueWork(context, myService);

        }else{

            context.startService(myService);
        }
    }
}
