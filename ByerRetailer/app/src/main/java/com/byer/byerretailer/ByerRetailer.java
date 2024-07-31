package com.byer.byerretailer;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ByerRetailer extends Application {

    public static DatabaseReference retailerRef,productsRef,geoRef,catalogueItemRequestRef,userRef,statusRef,partnerRef;


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

        // firebase components
        catalogueItemRequestRef=FirebaseDatabase.getInstance().getReference().child("Catalogue Requests");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        retailerRef=FirebaseDatabase.getInstance().getReference().child("Retailers");
        productsRef=FirebaseDatabase.getInstance().getReference().child("Products");
        statusRef=FirebaseDatabase.getInstance().getReference().child("Status");
        geoRef=FirebaseDatabase.getInstance().getReference().child("GeoFire");
        partnerRef=FirebaseDatabase.getInstance().getReference().child("Partners");

    }
}
