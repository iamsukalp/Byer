package com.byer.byerdeliverypartner.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;

import com.byer.byerdeliverypartner.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.white));

        setContentView(R.layout.activity_main);


    }

}