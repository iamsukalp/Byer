package com.flashotech.byerconsole;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ByerConsole extends Application {

    public static FirebaseAuth mAuth;
    public static DatabaseReference catalogueRef,catalogueItemRequestRef,menuRef,distanceRef;
    public static StorageReference storageReference;
    public static String current_user;
    public static String two, two_to_three,three_to_five;

    @Override
    public void onCreate() {
        super.onCreate();

        // Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // firebase components
        mAuth=FirebaseAuth.getInstance();
        current_user=mAuth.getCurrentUser().getUid();
        catalogueItemRequestRef=FirebaseDatabase.getInstance().getReference().child("Catalogue Requests");
        catalogueRef= FirebaseDatabase.getInstance().getReference().child("Catalogue");
        menuRef= FirebaseDatabase.getInstance().getReference().child("Menu");
        storageReference= FirebaseStorage.getInstance().getReference();


        distanceRef=FirebaseDatabase.getInstance().getReference().child("Distance");
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
