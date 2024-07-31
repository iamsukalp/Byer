package com.byer.byer.Utilities;

import com.google.firebase.auth.FirebaseAuth;

public class Constants {
    public static FirebaseAuth mAuth;
    public static String userId;
    public static final String FCM_SERVER_KEY="AAAAMgXxz1c:APA91bHhw-ic_n64SRoNzIKbEFI6VHQojXtU-534FPuYgH7NMvmU_yPt8lapD8eilfpfANSW9RvHP1dXruUj6UWT7PBeGdmJR140EVCYiX8o7vrBwpxDofbPPvs217dZHINJlHnF9wx2";




    public static void getConstants(){
        mAuth=FirebaseAuth.getInstance();
        if(mAuth!=null){
            userId=mAuth.getCurrentUser().getUid();
        }

    }


}