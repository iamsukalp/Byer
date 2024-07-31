package com.byer.byer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.byer.byer.Fragments.HomeFragment;
import com.byer.byer.Fragments.OrderFragment;
import com.byer.byer.Fragments.WalletFragment;
import com.byer.byer.R;
import com.byer.byer.databinding.ActivityHomeBinding;
import com.byer.byer.databinding.TopDownMenuBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.byer.byer.Byer.geoRef;
import static com.byer.byer.Byer.userRef;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityHomeBinding binding;
    private TopDownMenuBinding menuBinding;
    private FirebaseAuth mAuth;
    private String userId;
    private LocationManager locationManager;

    private FragmentManager fragmentManager;


    private ConstraintSet constraintSetShow= new ConstraintSet();
    private ConstraintSet constraintSetHide=new ConstraintSet();
    private boolean isMenuOpen=false;
    private int REQUEST_LOCATION=1;
    private double lat,longi;
    private GeoFire geoFire;
    public static String userName;
    private String sublocality,address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        menuBinding=TopDownMenuBinding.bind(binding.getRoot());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        init();
        initializeBottomBar();
        initializeListeners();
        getUserData();


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull @NotNull Task<String> task) {
                Map<String,Object> map=new HashMap<>();
                map.put("token",task.getResult());

                userRef.child(userId).updateChildren(map);
            }
        });

    }

    private void initializeListeners() {
        //buttons
        binding.homeMenuOpenBtn.setOnClickListener(this);
        binding.homeMenuCloseBtn.setOnClickListener(this);
        binding.homeNotification.setOnClickListener(this);

        binding.homeCart.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getCartBadge();
    }

    private void getCartBadge() {
        userRef.child(userId).child("Cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    binding.homeCart.setEnabled(true);
                                    binding.homeCartBadge.setText(snapshot.getChildrenCount()+"");
                                    binding.homeCartBadge.setEnabled(true);
                                    binding.homeCartBadge.setVisibility(View.VISIBLE);
                                }
                                else{
                                    binding.homeCart.setImageTintList(ColorStateList.valueOf(getColor(R.color.grey)));
                                    binding.homeCart.setEnabled(false);
                                    binding.homeCartBadge.setEnabled(false);
                                    binding.homeCartBadge.setVisibility(View.INVISIBLE);
                                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getUserData() {
            userRef.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("image")){
                                Picasso.get().load(snapshot.child("image").getValue().toString().trim())
                                        .placeholder(R.drawable.ic_profile_placeholder).networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(menuBinding.menuProfileIV, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get().load(snapshot.child("image").getValue().toString().trim())
                                                        .placeholder(R.drawable.ic_profile_placeholder)
                                                        .into(menuBinding.menuProfileIV);
                                            }
                                        });
                            }
                            else{
                                menuBinding.menuProfileIV.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this,R.drawable.ic_profile_placeholder));
                            }

                            menuBinding.menuPhoneNumberTV.setText(snapshot.child("phone").getValue().toString().trim());

                            if (snapshot.hasChild("sublocality")){
                                sublocality=snapshot.child("sublocality").getValue().toString();
                                menuBinding.menuLocationTV.setText(snapshot.child("sublocality").getValue().toString().trim());
                            }
                            else{
                                sublocality=null;
                                // Location initiation
                                ActivityCompat.requestPermissions( HomeActivity.this,
                                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

                                findLocation();

                            }
                            userName=snapshot.child("name").getValue().toString().trim();
                            menuBinding.menuUserNameTV.setText(userName);
                            menuBinding.menuMailTV.setText(snapshot.child("email").getValue().toString().trim());

                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void findLocation() {
        Log.d("Location", "findLocation: ");
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            Log.d("Location", "findLocation: GPS is on");
            setGeoLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(sublocality)){
           // findLocation();
        }

    }

    private void setGeoLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                lat = locationGPS.getLatitude();
                longi = locationGPS.getLongitude();

                geoFire=new GeoFire(geoRef.child("Users"));

                geoFire.setLocation(userId, new GeoLocation(lat, longi), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error==null){

                            Geocoder gcd = new Geocoder(HomeActivity.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = gcd.getFromLocation(lat, longi, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            assert addresses != null;
                            if (addresses.size() > 0) {
                                sublocality=addresses.get(0).getSubLocality();
                                address=addresses.get(0).getAddressLine(0);

                                if (sublocality==null){
                                    sublocality=address;
                                }
                                Map<String,Object> map=new HashMap<>();
                                map.put("sublocality",sublocality);
                                map.put("address",address);

                                userRef.child(userId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d("Location Insert", "onComplete: Done ");
                                        }
                                        else{
                                            Log.d("Location Insert", "onComplete: Not Done ");
                                        }
                                    }
                                });

                            }
                            else {
                                Toast.makeText(HomeActivity.this, "Error Getting Geolocation", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else{
                            Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                Toast.makeText(HomeActivity.this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_LOCATION){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                setGeoLocation();
            }
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void initializeBottomBar() {
        binding.bottomBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onItemSelected(int id) {
                Fragment fragment = null;
                switch (id) {
                    case R.id.home:

                        fragment = new HomeFragment();

                        break;
                    case R.id.order:

                        fragment = new OrderFragment();


                        break;
                    case R.id.wallet:

                        fragment = new WalletFragment();
                        break;
                    default:
                        fragment = new HomeFragment();
                        break;
                }

                if (fragment != null) {
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.framelayout, fragment)
                            .commit();
                } else {
                    Log.e("Home Fragment", "Error in creating fragment");
                }
            }
        });
    }

    private void init() {

        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

        binding.bottomBar.setItemSelected(R.id.home, true);
        fragmentManager = getSupportFragmentManager();
        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.framelayout, homeFragment)
                .commit();


        constraintSetShow.clone(this,R.layout.activity_home_menu);
        constraintSetHide.clone(binding.layout);


        // location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.home_menu_close_btn:{
                    closeMenu();
                    break;
                }
                case R.id.home_menu_openBtn:{
                    openMenu();
                    break;
                }
                case R.id.home_notification:{
                    Intent intent =new Intent(this,NotificationActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.home_cart:{
                    Intent intent=new Intent(this,CartActivity.class);
                    startActivity(intent);
                    break;
                }
            }
    }

    private void openMenu() {
        getWindow().setStatusBarColor(getColor(R.color.logo_color));
        TransitionManager.beginDelayedTransition(binding.layout);
        constraintSetShow.applyTo(binding.layout);
        isMenuOpen=true;
    }

    private void closeMenu() {
        getWindow().setStatusBarColor(getColor(R.color.white));
        TransitionManager.beginDelayedTransition(binding.layout);
        constraintSetHide.applyTo(binding.layout);
        isMenuOpen=false;
    }


    @Override
    public void onBackPressed() {
        if (isMenuOpen){
            closeMenu();
        }
        else{
            super.onBackPressed();
        }
    }
}