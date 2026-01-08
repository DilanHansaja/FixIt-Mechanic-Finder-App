package com.dilanhansaja.fixit.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;


public class MyNotification {

    private static MyNotification myNotification;
    private NotificationManager notificationManager;

    private MyNotification() {}

    public static MyNotification getMyNotification() {
        if(myNotification==null){
            myNotification=new MyNotification();
        }

        return myNotification;
    }

    public void createNotificationChannel(Context context){

        notificationManager = context.getSystemService(NotificationManager.class);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(
                    "c1",
                    "channel1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationManager.createNotificationChannel(channel);

        }

    }

    public void sendNotification(Context context,String title,String description,int icon){

//        Intent intent = new Intent(context, HomeActivity.class);

//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                100,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE
//
//        );
//
//        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
//                R.drawable.notification,
//                "Goto FixIt",
//                pendingIntent
//        ).build();

        Notification notification = new NotificationCompat.Builder(context,"c1")
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1,notification);
    }
}



