package com.byer.byerretailer.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.byer.byerretailer.ByerRetailer.retailerRef;

public class Constants {

    public static final String FCM_SERVER_KEY="AAAAMgXxz1c:APA91bHhw-ic_n64SRoNzIKbEFI6VHQojXtU-534FPuYgH7NMvmU_yPt8lapD8eilfpfANSW9RvHP1dXruUj6UWT7PBeGdmJR140EVCYiX8o7vrBwpxDofbPPvs217dZHINJlHnF9wx2";
    public static final String MAPVIEW_BUNDLE_KEY="AIzaSyDpFIEf6Hti5aSWP2vgWPKIqOrs296Cr0w";
    public static final String ORDER_ACCEPTED_IMAGE="https://firebasestorage.googleapis.com/v0/b/byer-retailer.appspot.com/o/status%2Forder_accepted.png?alt=media&token=1e1df79b-f489-49c4-af4a-9849004a7407";
    public static FirebaseAuth mAuth;
    public static String retailerId;
    public static String category;





    public static void getConstants(){
        mAuth=FirebaseAuth.getInstance();
        if(mAuth!=null){
            retailerId=mAuth.getCurrentUser().getUid();
            Log.d("CONSTANTS", "getConstants: "+retailerId);
        }

        if (retailerId!=null){
            getRetailerDetailConstants();
        }

    }
    public static void getRetailerDetailConstants(){
        retailerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            if (dataSnapshot.hasChild(retailerId)){
                                category=dataSnapshot.getKey();
                       //         category=dataSnapshot.child(retailerId).child("category").getValue().toString();
                                Log.d("Category", "onDataChange: "+category);
                            }
                        }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}
