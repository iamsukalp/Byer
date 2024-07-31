package com.byer.byer;

import android.app.Application;

import androidx.annotation.NonNull;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class Byer extends Application {

    public static DatabaseReference userRef,geoRef,menuRef,retailerRef,productRef,distanceRef,subcategoryRef,orderRef;
    public static StorageReference storageReference;
    public static String two,two_to_three,three_to_five;

    @Override
    public void onCreate() {
        super.onCreate();


        // Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Picasso offline Capability
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        //firebase components
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        retailerRef= FirebaseDatabase.getInstance().getReference().child("Retailers");
        menuRef= FirebaseDatabase.getInstance().getReference().child("Menu");
        geoRef=FirebaseDatabase.getInstance().getReference().child("GeoFire");
        productRef=FirebaseDatabase.getInstance().getReference().child("Products");
        storageReference= FirebaseStorage.getInstance().getReference();

        distanceRef=FirebaseDatabase.getInstance().getReference().child("Distance");
        subcategoryRef=FirebaseDatabase.getInstance().getReference().child("Subcategories");
        orderRef=FirebaseDatabase.getInstance().getReference().child("Orders");

        // firebase cache persistence

        // getting delivery charge
        distanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    two=snapshot.child("twoKiloM").getValue().toString();
                    two_to_three=snapshot.child("twoToThreeKiloM").getValue().toString();
                    three_to_five=snapshot.child("threeToFiveKiloM").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
