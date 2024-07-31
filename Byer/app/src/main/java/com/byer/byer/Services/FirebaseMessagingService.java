package com.byer.byer.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.byer.byer.Activities.HomeActivity;
import com.byer.byer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationManager mNotificationManager;
    FirebaseAuth mAuth;
    DatabaseReference userRef;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


// playing audio and vibration when user se request
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(false);
        }

        // vibration
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        v.vibrate(pattern, -1);


      //  int resourceImage = getResources().getIdentifier(remoteMessage.getNotification().getIcon(), "drawable", getPackageName());



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "BYER_NOTIFICATIONS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        //    builder.setSmallIcon(resourceImage);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_icon);
          //  builder.setSmallIcon(resourceImage);
        }



        Intent resultIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        builder.setVibrate(pattern);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "BYER_NOTIFICATIONS";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }



// notificationId is a unique int for each notification that you must define
        mNotificationManager.notify(100, builder.build());


    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);

        mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        if (mAuth.getCurrentUser()!=null){
            Map<String,Object> map=new HashMap<>();
            map.put("token",s);
            userRef.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
        }


    }
}

