package com.flashotech.byerconsole.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flashotech.byerconsole.R;
import com.flashotech.byerconsole.databinding.FragmentSettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import static com.flashotech.byerconsole.ByerConsole.distanceRef;
import static com.flashotech.byerconsole.ByerConsole.three_to_five;
import static com.flashotech.byerconsole.ByerConsole.two;
import static com.flashotech.byerconsole.ByerConsole.two_to_three;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private String twoKiloM,threeToFiveKiloM,twoToThreeKiloM;

    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentSettingsBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {

    }

    @Override
    public void onStart() {
        super.onStart();
        getDeliveryCharges();
    }

    private void getDeliveryCharges() {
            binding.deliveryCharge2km.setHint("Current delivery charge up to 2 KM: "+two);
        binding.deliveryCharge2kmto3km.setHint("Current delivery charge for 2 to 3 KM: "+two_to_three);
        binding.deliveryCharge3kmto5km.setHint("Current delivery charge for 3to 5 KM: "+three_to_five);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            binding.deliveryChargeSaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validate()){
                        Map<String,String> map=new HashMap<>();
                        map.put("twoKiloM",twoKiloM);
                        map.put("twoToThreeKiloM",twoToThreeKiloM);
                        map.put("threeToFiveKiloM",threeToFiveKiloM);

                        distanceRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Snackbar.make(binding.deliveryChargeLayout,"Updated Successfully",Snackbar.LENGTH_SHORT).show();
                                }
                                else{
                                    Snackbar.make(binding.deliveryChargeLayout,"Unsuccessful",Snackbar.LENGTH_INDEFINITE).show();
                                }
                            }
                        });
                    }
                }
            });
    }

    private boolean validate() {
        twoKiloM=binding.deliveryCharge2km.getText().toString().trim();
        twoToThreeKiloM=binding.deliveryCharge2kmto3km.getText().toString().trim();
        threeToFiveKiloM=binding.deliveryCharge3kmto5km.getText().toString().trim();

        if (TextUtils.isEmpty(twoKiloM)){

            Snackbar.make(binding.deliveryChargeLayout,"Please enter valid inputs",Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(twoToThreeKiloM) ){

            Snackbar.make(binding.deliveryChargeLayout,"Please enter valid inputs",Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(threeToFiveKiloM)){

            Snackbar.make(binding.deliveryChargeLayout,"Please enter valid inputs",Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}