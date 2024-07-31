package com.byer.byerretailer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.ActivityServicesBinding;

public class ServicesActivity extends AppCompatActivity {

    private static final String SERVICE_TAG="service";
    private ActivityServicesBinding binding;
    private int service;
    private int[] title_image={
        R.drawable.delivery,
        R.drawable.accounting,
        R.drawable.managment,
        R.drawable.marketing
    };

    private String[] title_text={
            "By using delivery management retailers can manage their own delivery service and if they want they can add BYER delivery partner service for provide better delivery service to their customer",
            "It will help retailers to manage the daily transaction with regular customer and it will help retailers to remove the chances of mismatch of monthly transaction.",
            "Create offers for your customer and share it in different platform and provide real time offers to customer. (When customer enter to your shop, he will get the welcome message and special offers that you want to provide to them )",
            "FIRST SELL THEN PAY where you don’t need to pay the money immediately, you can buy now and pay letter. ( Free credit limit 15 days otherwise interest is applicable )",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

    }

    private void init() {
        // getting data from intent
        service=getIntent().getIntExtra(SERVICE_TAG,0);

        // showing details
        binding.servicesTitleImage.setImageDrawable(getDrawable(title_image[service]));
        binding.servicesTitleText.setText(title_text[service]);
    }
}