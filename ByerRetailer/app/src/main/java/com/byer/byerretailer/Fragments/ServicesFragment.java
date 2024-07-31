package com.byer.byerretailer.Fragments;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byer.byerretailer.Activities.ServicesActivity;
import com.byer.byerretailer.Activities.SubscriptionActivity;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.FragmentServicesBinding;

import org.jetbrains.annotations.NotNull;


public class ServicesFragment extends Fragment implements View.OnClickListener {

    private FragmentServicesBinding binding;
    private static final String SERVICE_TAG="service";

    public ServicesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentServicesBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void init() {
        binding.servicesDelivery.setOnClickListener(this);
        binding.servicesAccounting.setOnClickListener(this);
        binding.servicesManagement.setOnClickListener(this);
        binding.servicesMarketing.setOnClickListener(this);

        binding.servicesSubscription.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int i=0;
        switch (v.getId()){
            case R.id.services_delivery:{
                i=0;
                break;
            }
            case R.id.services_accounting:{
                i=1;
                break;
            }
            case R.id.services_management:{
                i=2;
                break;
            }
            case R.id.services_marketing: {
                i=3;
                break;
            }
            case R.id.services_subscription:{
                Intent intent =new Intent(requireActivity(), SubscriptionActivity.class);
                startActivity(intent);
                break;
            }
        }
        Intent intent = new Intent(requireActivity(), ServicesActivity.class);
        intent.putExtra(SERVICE_TAG,i);
        Pair[] pairs = new Pair[1];
        pairs[0]=new Pair<View,String>(v,"service_title_transition");
        ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(requireActivity(),pairs);
        startActivity(intent,options.toBundle());

    }
}