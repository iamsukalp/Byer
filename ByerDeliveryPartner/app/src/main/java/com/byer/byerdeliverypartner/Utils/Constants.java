package com.byer.byerdeliverypartner.Utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.byer.byerdeliverypartner.ByerPartner.partnerRef;

public class Constants {

    public static FirebaseAuth mAuth=FirebaseAuth.getInstance();
    public static String partnerId;
    public static String partnerName;
    public static String phone;
    public static String image;
    public static final int LOCATION_SERVICE_ID =173;
    public static final String ACTION_START_LOCATION_SERVICE="startLocationService";
    public static final String ACTION_STOP_LOCATION_SERVICE="stopLocationService";

    public static final String FCM_SERVER_KEY="AAAAMgXxz1c:APA91bHhw-ic_n64SRoNzIKbEFI6VHQojXtU-534FPuYgH7NMvmU_yPt8lapD8eilfpfANSW9RvHP1dXruUj6UWT7PBeGdmJR140EVCYiX8o7vrBwpxDofbPPvs217dZHINJlHnF9wx2";




}
