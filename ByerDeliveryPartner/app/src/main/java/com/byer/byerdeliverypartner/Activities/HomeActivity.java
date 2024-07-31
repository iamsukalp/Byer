package com.byer.byerdeliverypartner.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


import com.byer.byerdeliverypartner.BuildConfig;
import com.byer.byerdeliverypartner.R;
import com.byer.byerdeliverypartner.Services.LocationUpdatesIntentService;
import com.byer.byerdeliverypartner.Utils.Constants;
import com.byer.byerdeliverypartner.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byer.byerdeliverypartner.ByerPartner.partnerRef;


public class HomeActivity extends AppCompatActivity{

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private GradientDrawable seekBarBackground;
    private FirebaseAuth mAuth;
    private String partnerId;
    private String image,name,phone;
    private CircleImageView partnerImage;
    private TextView partnerName,partnerPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        getWindow().setNavigationBarColor(getResources().getColor(R.color.white,null));
        setContentView(binding.getRoot());

        init();
        getPartnerDetails();


        binding.navOnDutySwitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (isLocationServiceRunning()) {
                    if (seekBar.getProgress() >= 80) {
                        seekBar.setProgress(0, true);
                        getPartnerLocation(isLocationServiceRunning());
                    } else{
                        seekBar.setProgress(0, true);
                    }


                } else {

                    if (seekBar.getProgress() >= 80) {
                        seekBar.setProgress(0, true);
                        getPartnerLocation(isLocationServiceRunning());
                    } else {
                        seekBar.setProgress(0, true);
                    }

                }

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void getPartnerDetails() {
        partnerRef.child(partnerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                           name=snapshot.child("name").getValue().toString();
                           phone=snapshot.child("phone").getValue().toString();
                           image=snapshot.child("image").getValue().toString();

                           Picasso.get()
                                   .load(image)
                                   .placeholder(R.drawable.ic_profile_placeholder)
                                   .networkPolicy(NetworkPolicy.OFFLINE)
                                   .into(partnerImage, new Callback() {
                                       @Override
                                       public void onSuccess() {

                                       }

                                       @Override
                                       public void onError(Exception e) {
                                           Picasso.get()
                                                   .load(image)
                                                   .placeholder(R.drawable.ic_profile_placeholder)
                                                   .into(partnerImage);
                                       }
                                   });

                           partnerName.setText(name);
                           partnerPhone.setText(phone);
                    }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(){



        setSupportActionBar(binding.appBarHome.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View header = navigationView.getHeaderView(0);
        partnerImage=header.findViewById(R.id.nav_Image);
        partnerName=header.findViewById(R.id.nav_Name);
        partnerPhone=header.findViewById(R.id.nav_Phone);

        navigationView.setItemIconTintList(null);
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigationView);
        bottomNavigationView.setItemIconTintList(null);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.settingsFragment, R.id.referAPartnerFragment, R.id.accountBalanceFragment,R.id.ordersFragment,R.id.homeFragment,R.id.profileFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.home_nav_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        NavigationUI.setupWithNavController(bottomNavigationView,navController);

        seekBarBackground= (GradientDrawable) binding.navOnDutySwitch.getBackground();


        if (isLocationServiceRunning()){
            binding.navOnDutySwitch.setThumb(getResources().getDrawable(R.drawable.ic_slider_stop, null));
            binding.navOnDutyLabel.setTextColor(getResources().getColor(R.color.payment_failed_bg, null));
            binding.navOnDutyLabel.setText("Slide to go off duty →");
            seekBarBackground.setStroke(2,getResources().getColor(R.color.payment_failed_bg,null));
        }
        else{
            binding.navOnDutySwitch.setThumb(getResources().getDrawable(R.drawable.ic_slider_start, null));
            binding.navOnDutyLabel.setTextColor(getResources().getColor(R.color.logo_color, null));
            binding.navOnDutyLabel.setText("Slide to go on duty →");
            seekBarBackground.setStroke(2,getResources().getColor(R.color.logo_color,null));
        }


        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        partnerId=mAuth.getCurrentUser().getUid();



        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull @NotNull Task<String> task) {
                Map<String,Object> map=new HashMap<>();
                map.put("token",task.getResult());

                partnerRef.child(partnerId).updateChildren(map);
            }
        });


    }


    private void getPartnerLocation(boolean isOnDuty) {
        if (isLocationServiceRunning()){
            stopLocationService();
        }
        else{
                  if (!checkPermissions()) {
                     requestPermissions();
                 }
                  else{
                       startLocationService();
                  }
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        Log.d(TAG, "checkPermissions: "+permissionState);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager= (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
                for (ActivityManager.RunningServiceInfo serviceInf:
                activityManager.getRunningServices(Integer.MAX_VALUE)){
                    if (LocationUpdatesIntentService.class.getName().equals(serviceInf.service.getClassName())){
                        if (serviceInf.foreground){
                            return true;
                        }
                    }
                }
                return false;
        }
        return false;

    }

    private void startLocationService(){
        if (!isLocationServiceRunning()){
            Intent intent=new Intent(getApplicationContext(), LocationUpdatesIntentService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);

        }
        binding.navOnDutySwitch.setThumb(getResources().getDrawable(R.drawable.ic_slider_stop, null));
        binding.navOnDutyLabel.setTextColor(getResources().getColor(R.color.payment_failed_bg, null));
        binding.navOnDutyLabel.setText("Slide to go off duty →");
        seekBarBackground.setStroke(2,getResources().getColor(R.color.payment_failed_bg,null));
    }

    private void stopLocationService(){
        if (isLocationServiceRunning()){
            Intent intent =new Intent(getApplicationContext(),LocationUpdatesIntentService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);

        }
        binding.navOnDutySwitch.setThumb(getResources().getDrawable(R.drawable.ic_slider_start, null));
        binding.navOnDutyLabel.setTextColor(getResources().getColor(R.color.logo_color, null));
        binding.navOnDutyLabel.setText("Slide to go on duty →");
        seekBarBackground.setStroke(2,getResources().getColor(R.color.logo_color,null));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.home_nav_fragment);
        return NavigationUI.navigateUp(navController,mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }

    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            Snackbar.make(
                    findViewById(R.id.drawer_layout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {



            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, do your thang

                Log.d(TAG, "onRequestPermissionsResult: here");
                    startLocationService();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

}