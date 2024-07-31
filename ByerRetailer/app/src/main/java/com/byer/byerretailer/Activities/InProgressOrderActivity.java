package com.byer.byerretailer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.byer.byerretailer.Adapters.ItemAdapter;
import com.byer.byerretailer.Adapters.StatusAdapter;
import com.byer.byerretailer.Models.ItemModel;
import com.byer.byerretailer.Models.StatusModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.ActivityInProgressOrderBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.byer.byerretailer.ByerRetailer.geoRef;
import static com.byer.byerretailer.ByerRetailer.retailerRef;
import static com.byer.byerretailer.Utils.Constants.MAPVIEW_BUNDLE_KEY;
import static com.byer.byerretailer.Utils.Constants.retailerId;

public class InProgressOrderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String ORDER_ID_TAG="orderId";
    private ActivityInProgressOrderBinding binding;
    private boolean isItemListShown=false;
    private List<StatusModel> statusList=new ArrayList<>();
    private List<String> statusListKey=new ArrayList<>();
    private List<ItemModel> itemList=new ArrayList<>();
    private StatusAdapter statusAdapter;
    private ItemAdapter itemAdapter;
    private String orderId;
    public static String userId;
    private String userName;
    private LinearLayoutManager statusLinearLayoutManager=new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false);
    private LinearLayoutManager itemLinearLayoutManager=new LinearLayoutManager(this);

    private GeoFire geoFire;
    public static double userLat,userLong,shopLat,shopLong;
    private GoogleMap map;
    private FirebaseAuth mAuth;
    public static String retailerId;
    public static String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInProgressOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        init();
        binding.inProgressMap.onCreate(mapViewBundle);


        binding.inProgressMap.getMapAsync(this);




        binding.inProgressItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isItemListShown=true;
                        binding.inProgressTransformationLayout.startTransform();

                    }
                });
                 }
        });



    }

    private void init() {

        //  get order id from intent
        orderId=getIntent().getStringExtra(ORDER_ID_TAG);

        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        retailerId=mAuth.getCurrentUser().getUid();

        statusAdapter=new StatusAdapter(this,statusList,orderId,statusListKey);
        itemAdapter=new ItemAdapter(this,itemList);

        // setting up status list recycler view
        statusLinearLayoutManager.isAutoMeasureEnabled();
        binding.inProgressStatusList.setHasFixedSize(true);
        binding.inProgressStatusList.setLayoutManager(statusLinearLayoutManager);
        binding.inProgressStatusList.setAdapter(statusAdapter);

        // setting up item list adapter
        itemLinearLayoutManager.isAutoMeasureEnabled();
        binding.inProgressItemList.setHasFixedSize(true);
        binding.inProgressItemList.setLayoutManager(itemLinearLayoutManager);
        binding.inProgressItemList.setAdapter(itemAdapter);

        // map components


        // firebase components
        getRetailerLocation();
    }

    private void getRetailerLocation() {
        geoFire = new GeoFire(geoRef.child("Retailers"));
        geoFire.getLocation(retailerId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                shopLat=location.latitude;
                shopLong=location.longitude;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getUserLocation() {
        geoFire = new GeoFire(geoRef.child("Users"));
        geoFire.getLocation(userId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                userLat=location.latitude;
                userLong=location.longitude;

                map.addMarker(new MarkerOptions()
                        .position(new LatLng(21.90, 84))
                        .anchor(0.5f, 0.5f)
                        .title(userName)
                        .icon(bitmapDescriptorFromVector(InProgressOrderActivity.this, R.drawable.ic_home_location)));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        binding.inProgressMap.onSaveInstanceState(mapViewBundle);
    }


    @Override
        public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        map=googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
     //   googleMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
        Log.d("MAP", "onMapReady: "+shopLat+"  "+shopLong);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(shopLat, shopLong), 14.0f));
    }
    @Override
    public void onResume() {
        super.onResume();
        binding.inProgressMap.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.inProgressMap.onStart();
        getData();
    }

    private void getData() {


            retailerRef.child(Constants.category).child(retailerId).child("Orders").child(orderId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                // get basic order details here
                                userId=snapshot.child("userId").getValue().toString();
                                userName= snapshot.child("userName").getValue().toString();
                                getUserLocation();

                                // getting status details here
                                statusList.clear();
                                statusListKey.clear();
                                itemList.clear();

                                for (DataSnapshot dataSnapshot:snapshot.child("RetailerStatus").getChildren()){
                                    String key=dataSnapshot.getKey();

                                    StatusModel model=dataSnapshot.getValue(StatusModel.class);
                                    statusList.add(model);
                                    statusListKey.add(key);
                                }
                                // get order items here
                                statusAdapter.notifyDataSetChanged();

                                for(DataSnapshot dataSnapshot:snapshot.child("Items").getChildren()){
                                    ItemModel model=dataSnapshot.getValue(ItemModel.class);
                                    itemList.add(model);
                                }
                                itemAdapter.notifyDataSetChanged();
                            }
                            else{
                                statusAdapter.notifyDataSetChanged();
                                itemAdapter.notifyDataSetChanged();
                            }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
    }


    @Override
    public void onStop() {
        super.onStop();
        binding.inProgressMap.onStop();
    }
    @Override
    public void onPause() {
        binding.inProgressMap.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        binding.inProgressMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.inProgressMap.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (isItemListShown){
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  isItemListShown=false;
                  binding.inProgressTransformationLayout.finishTransform();
              }
          });
        }else{
            super.onBackPressed();
        }

    }
}