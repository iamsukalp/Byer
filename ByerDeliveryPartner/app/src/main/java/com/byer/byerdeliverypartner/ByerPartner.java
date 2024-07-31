package com.byer.byerdeliverypartner;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ByerPartner extends Application {
    public static DatabaseReference partnerRef,geoRef;
    public static StorageReference storageReference;

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
        partnerRef=FirebaseDatabase.getInstance().getReference().child("Partners");
        geoRef= FirebaseDatabase.getInstance().getReference().child("GeoFire");
        storageReference= FirebaseStorage.getInstance().getReference();




    }
}
