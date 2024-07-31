package com.byer.byerretailer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.byer.byerretailer.databinding.ActivitySubscriptionBinding;

public class SubscriptionActivity extends AppCompatActivity {

    private ActivitySubscriptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

    }

    private void init() {

    }
}