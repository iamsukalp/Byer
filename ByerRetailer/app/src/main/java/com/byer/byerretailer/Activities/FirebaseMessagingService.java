package com.byer.byerretailer.Activities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageSent(@NonNull @NotNull String s) {
        super.onMessageSent(s);

        Log.d("Message Sent",s);

    }

    @Override
    public void onSendError(@NonNull @NotNull String s, @NonNull @NotNull Exception e) {
        super.onSendError(s, e);
        Log.d("Message Error",e.getMessage());
    }
}
