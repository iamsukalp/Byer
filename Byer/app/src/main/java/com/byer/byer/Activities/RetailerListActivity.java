package com.byer.byer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.geofire.util.GeoUtils;
import com.byer.byer.Adapters.RetailerListAdapter;
import com.byer.byer.Models.RetailerModel;
import com.byer.byer.R;
import com.byer.byer.databinding.ActivityRetailerListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.byer.byer.Byer.geoRef;
import static com.byer.byer.Byer.retailerRef;


public class RetailerListActivity extends AppCompatActivity {

    private ActivityRetailerListBinding binding;
    private FirebaseAuth mAuth;
    private String userId;
    private GeoFire geoFire;
    private double userLat,userLon;
    private String category;
    private List<String> keyList = new ArrayList<>();
    private List<RetailerModel> retailerList = new ArrayList<>();
    private List<RetailerModel> retailerListAll = new ArrayList<>();
    private List<String> distanceList=new ArrayList<>();
    private RetailerListAdapter retailerListAdapter=new RetailerListAdapter(this,keyList,retailerList,retailerListAll,distanceList,category);
    private LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRetailerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getLocations();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu,menu);
        MenuItem item=menu.findItem(R.id.appbar_search);
        SearchView searchView= (SearchView) item.getActionView();
        EditText searchEditText = searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.BLACK);
        ImageView imvClose = searchView.findViewById(R.id.search_close_btn);
        imvClose.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close_color));
        searchView.setBackground(ContextCompat.getDrawable(this,R.drawable.bottombar_bg_logo));
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.appbarLayout.setExpanded(false);
            }
        });


        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                binding.appbarLayout.setExpanded(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                retailerListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


          retailerListAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void getLocations() {
        geoFire=new GeoFire(geoRef.child("Users"));
        geoFire.getLocation(userId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                Log.d("Userkey", "onLocationResult: "+key);
                userLat=location.latitude;
                userLon=location.longitude;

                Log.d("User Location", "onLocationResult: "+userLat+"|"+userLon);

                getRetailerList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getRetailerList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                geoFire = new GeoFire(geoRef.child("Retailers"));

                geoFire.queryAtLocation(new GeoLocation(userLat, userLon), 5).addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        double lat=location.latitude;
                        double longi=location.longitude;

                        distanceList.clear();
                        keyList.clear();
                        retailerListAll.clear();
                        retailerList.clear();

                       String distance= String.valueOf(Math.round(GeoUtils.distance(userLat,userLon,lat,longi)/1000));
                        distanceList.add(distance);
                        Log.d("KEYVALUE_TAG", "onClick: "+key);
                        keyList.add(key);
                        retailerRef.child(category).child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //keyArray.add(snapshot.getKey());
                                RetailerModel retailerModel=snapshot.getValue(RetailerModel.class);
                                retailerList.add(retailerModel);
                                retailerListAll.add(retailerModel);

                                retailerListAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
            }
        });
    }

    private void init() {
        //firebase authentication
        mAuth=FirebaseAuth.getInstance();
        userId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        category=getIntent().getStringExtra("Category");


        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle("Retailers near you");
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.black));
        binding.toolbar.setCollapseIcon(ContextCompat.getDrawable(this,R.drawable.ic_back));
        binding.collapsedToolbar.setTitle("Retailers near you");
        binding.collapsedToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.black));
        
        // recycler set up
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.retailerList.setHasFixedSize(true);
        binding.retailerList.setNestedScrollingEnabled(false);
        binding.retailerList.setLayoutManager(linearLayoutManager);
        binding.retailerList.setAdapter(retailerListAdapter);


    }


}