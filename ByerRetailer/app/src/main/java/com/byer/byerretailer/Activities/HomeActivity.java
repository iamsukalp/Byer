package com.byer.byerretailer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.byer.byerretailer.ByerRetailer;
import com.byer.byerretailer.Dialogs.ChangeShopAvailabilityDialog;
import com.byer.byerretailer.Fragments.HomeFragment;
import com.byer.byerretailer.Fragments.OrdersFragment;
import com.byer.byerretailer.Fragments.ServicesFragment;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.ActivityHomeBinding;
import com.byer.byerretailer.databinding.TopDownMenuBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnDismissListener {

    private static final int REQUEST_LOCATION = 1;
    private ActivityHomeBinding binding;
    private TopDownMenuBinding menuBinding;
    FragmentManager fragmentManager;
    private boolean isMenuOpen=false;
    private boolean isShopOpen=false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ChangeShopAvailabilityDialog changeShopAvailabilityDialog;
    private static final String SHOP_PREF_TAG= "isShopOpen";
    private static final String PREF_TAG= "MyPref";
    private static final String CATEGORY_TAG= "Category";

    private FirebaseAuth mAuth;
    private DatabaseReference retailerRef,geoRef;
    private String name,retailerId,phone,email,image;
    private String sublocality=null;
    public static String category;

    private GeoFire geoFire;
    private double lat,longi;
    private String address;
    private LocationManager locationManager;
    private int orderBadge;


    private ConstraintSet constraintSetShow= new ConstraintSet();
    private ConstraintSet constraintSetHide=new ConstraintSet();

    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        menuBinding=TopDownMenuBinding.bind(binding.getRoot());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        init(savedInstanceState);

        getRetailerDetails();
        initListeners();


        binding.bottomBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onItemSelected(int id) {
                Fragment fragment = null;
                switch (id) {
                    case R.id.home:
                        fragment = new HomeFragment();
                        binding.homeAvailabilitySwitchContainer.setVisibility(View.VISIBLE);
                        binding.homeAvailabilitySwitchContainer.setEnabled(true);

                        break;
                    case R.id.order:
                        fragment = new OrdersFragment();
                        binding.homeAvailabilitySwitchContainer.setVisibility(View.INVISIBLE);
                        binding.homeAvailabilitySwitchContainer.setEnabled(false);

                        break;
                    case R.id.services:
                        fragment = new ServicesFragment();
                        binding.homeAvailabilitySwitchContainer.setVisibility(View.INVISIBLE);
                        binding.homeAvailabilitySwitchContainer.setEnabled(false);

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



        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull @NotNull Task<String> task) {
                Map<String,Object> map=new HashMap<>();
                map.put("token",task.getResult());

                retailerRef.child(retailerId).updateChildren(map);
            }
        });


    }

    private void getOrdersBadge() {
        ByerRetailer.retailerRef.child(category).child(retailerId).child("Orders").orderByChild("status").equalTo("new").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        orderBadge= Math.toIntExact(snapshot.getChildrenCount());
                    }
                    else{
                        orderBadge= (int) snapshot.getChildrenCount();
                    }

                    binding.bottomBar.showBadge(R.id.order,orderBadge);
                }
                else {
                    binding.bottomBar.dismissBadge(R.id.order);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initListeners() {

        // bottom bar initial state expression


        //buttons
        binding.homeMenuOpenBtn.setOnClickListener(this);
        binding.homeMenuCloseBtn.setOnClickListener(this);
        binding.homeNotification.setOnClickListener(this);
        binding.homeAvailabilitySwitchContainer.setOnClickListener(this);

        // dialog
        changeShopAvailabilityDialog.setOnDismissListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        // get retailer details
        getNotifications();
        getOrdersBadge();


    }


    private void getNotifications() {
        ByerRetailer.catalogueItemRequestRef.child(retailerId).orderByChild("seen").equalTo(false).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    binding.homeNotificationBadge.setVisibility(View.VISIBLE);
                    binding.homeNotificationBadge.setText(snapshot.getChildrenCount()+"");
                }
                else{
                    binding.homeNotificationBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRetailerDetails() {
        retailerRef.child(category).child(retailerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name= Objects.requireNonNull(snapshot.child("name").getValue()).toString().trim();
                    image= Objects.requireNonNull(snapshot.child("image").getValue()).toString().trim();
                    if(snapshot.hasChild("phone")){
                        phone= Objects.requireNonNull(snapshot.child("phone").getValue()).toString().trim();
                    }else{
                        phone="Data Unavailable";
                    }
                    email= Objects.requireNonNull(snapshot.child("email").getValue()).toString().trim();

                    if (snapshot.hasChild("sublocality")){
                        sublocality=snapshot.child("sublocality").getValue().toString().trim();
                        menuBinding.menuLocationTV.setText(sublocality);
                    }
                    else{
                        getRetailerLocation();
                    }

                    menuBinding.menuUserNameTV.setText(name);
                    menuBinding.menuPhoneNumberTV.setText(phone);
                    menuBinding.menuMailTV.setText(email);
                    Picasso.get().load(image).placeholder(R.drawable.ic_profile_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(menuBinding.menuProfileIV, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.ic_profile_placeholder).into(menuBinding.menuProfileIV);
                        }
                    });



                }
                else{
                    Intent intent=new Intent(HomeActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRetailerLocation() {
        // find retailer's location

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            setGeoLocation();
        }
    }

    private void openShopAvailabilityDialog() {
        // Shop availability dialog definition

        changeShopAvailabilityDialog.show();
    }

    private void closeMenu() {

        getWindow().setStatusBarColor(getColor(R.color.white));
        TransitionManager.beginDelayedTransition(binding.layout);
        constraintSetHide.applyTo(binding.layout);
        binding.homeLogo.setImageTintList(ColorStateList.valueOf(getColor(R.color.logo_color)));
        isMenuOpen=false;
    }

    private void openMenu() {

        getWindow().setStatusBarColor(getColor(R.color.logo_color));
        TransitionManager.beginDelayedTransition(binding.layout);
        constraintSetShow.applyTo(binding.layout);
        binding.homeLogo.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
        isMenuOpen=true;
    }


    private void init(Bundle savedInstanceState) {

        // shared pref
        pref=this.getSharedPreferences(PREF_TAG,0);
        isShopOpen=pref.getBoolean(SHOP_PREF_TAG,false);
        category=pref.getString(CATEGORY_TAG,null);
        Log.d("PREFSS", "init: "+category);
        editor=pref.edit();



        changeShopAvailabilityDialog=new ChangeShopAvailabilityDialog(HomeActivity.this);

        if (isShopOpen){
            binding.homeAvailabilityLabel.setText("Close Shop");
            binding.homeAvailabilitySwitch.setMinAndMaxProgress(0.5f,1.0f);
            binding.homeAvailabilitySwitch.playAnimation();
            binding.homeAvailabilityLabel.setText("Close Shop");
        }
        else{
            binding.homeAvailabilityLabel.setText("Open Shop");
            binding.homeAvailabilitySwitch.setMinAndMaxProgress(0.0f,0.5f);
            binding.homeAvailabilitySwitch.playAnimation();
            binding.homeAvailabilityLabel.setText("Open Shop");
        }



        if (savedInstanceState == null) {
            binding.bottomBar.setItemSelected(R.id.home, true);
            fragmentManager = getSupportFragmentManager();
            HomeFragment homeFragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout, homeFragment)
                    .commit();
        }


        constraintSetShow.clone(this,R.layout.activity_home_menu);
        constraintSetHide.clone(binding.layout);

        // Firebase components
        mAuth=FirebaseAuth.getInstance();
        retailerId=mAuth.getCurrentUser().getUid();
        retailerRef= FirebaseDatabase.getInstance().getReference().child("Retailers");
        geoRef=FirebaseDatabase.getInstance().getReference().child("GeoFire").child("Retailers");

        retailerRef.keepSynced(true);
        geoRef.keepSynced(true);

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public void onBackPressed() {



        if (isMenuOpen){
            closeMenu();
        } else if(HomeFragment.filterOpen){
            HomeFragment.closeFilter();
        }
        else{
            super.onBackPressed();
        }

    }

    @SuppressLint("NonConstantResourceId")
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
            case R.id.home_availability_switch_container:{
                openShopAvailabilityDialog();
                break;
            }

        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShopOpen=pref.getBoolean(SHOP_PREF_TAG,false);
        Log.d("OnDialogClosed",isShopOpen+"");
        if (isShopOpen){
            binding.homeAvailabilitySwitch.setMinAndMaxProgress(0.5f,1.0f);
            binding.homeAvailabilitySwitch.playAnimation();
            binding.homeAvailabilityLabel.setText("Close Shop");


        }
        else{
            binding.homeAvailabilitySwitch.setMinAndMaxProgress(0.0f,0.5f);
            binding.homeAvailabilitySwitch.playAnimation();
            binding.homeAvailabilityLabel.setText("Open Shop");

        }
    }



    private void setGeoLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
        //    getLastLocation();

        }
    }

    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask=fusedLocationProviderClient.getLastLocation();
        locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                if (task.isSuccessful()){
                    Location location=task.getResult();
                    lat = location.getLatitude();
                    longi = location.getLongitude();

                    Log.d("Fused Location Home", "onComplete: "+lat+" "+longi);

                    geoFire=new GeoFire(geoRef);

                    geoFire.setLocation(retailerId, new GeoLocation(lat, longi), new GeoFire.CompletionListener() {
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

                }
            }


        });
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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



}