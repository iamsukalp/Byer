package com.byer.byerdeliverypartner.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.byer.byerdeliverypartner.Activities.HomeActivity;
import com.byer.byerdeliverypartner.R;
import com.byer.byerdeliverypartner.Utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;


import org.jetbrains.annotations.NotNull;

import static com.byer.byerdeliverypartner.ByerPartner.geoRef;


public class LocationUpdatesIntentService extends Service {

    private GeoFire geoFire;
    private FirebaseAuth mAuth;
    private String partnerId;

    @Override
    public void onCreate() {
        super.onCreate();
        geoFire=new GeoFire(geoRef.child("Partners"));
        mAuth=FirebaseAuth.getInstance();
        partnerId=mAuth.getCurrentUser().getUid();
    }

    private final LocationCallback locationCallback=new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {

            super.onLocationResult(locationResult);
            locationResult.getLastLocation();
            double latitude =locationResult.getLastLocation().getLatitude();
            double longitude =locationResult.getLastLocation().getLongitude();
            Log.d("Location Update", "lat: "+latitude+"  "+"Long: "+longitude);

            geoFire.setLocation(partnerId, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error!=null){
                        Log.d("Location Services", "onComplete: "+error.getMessage());
                    }
                    else{
                        Log.d("Location Services", "onComplete: "+ "done");
                    }
                }
            });

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @SuppressLint("MissingPermission")
    private void startLocationService(){
        String channelId="location_notification_channel";
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );

        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (notificationManager!=null
                    && notificationManager.getNotificationChannel(channelId)==null){
                NotificationChannel notificationChannel=new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This Channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest=new LocationRequest();
        locationRequest.setInterval(6000); // 15000
        locationRequest.setFastestInterval(4000);//10000
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID,builder.build());
    }

    private void stopLocationServices(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            String action=intent.getAction();
            if(action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                startLocationService();
            }
            else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                stopLocationServices();
            }
        }
        return Service.START_STICKY;
    }
}