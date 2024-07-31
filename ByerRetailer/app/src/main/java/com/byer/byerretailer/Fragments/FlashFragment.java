package com.byer.byerretailer.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byer.byerretailer.Activities.HomeActivity;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.FragmentFlashBinding;
import com.google.firebase.auth.FirebaseAuth;


public class FlashFragment extends Fragment {


    private FragmentFlashBinding binding;
    private FirebaseAuth mAuth;



    public FlashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFlashBinding.inflate(inflater, container, false);

        init();

        return binding.getRoot();

    }

    private void init() {

        mAuth=FirebaseAuth.getInstance();


        binding.flashAnim.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mAuth.getCurrentUser()!=null){
                    Constants.getConstants();
                    Intent intent = new Intent(requireActivity(), HomeActivity.class);
                    requireActivity().startActivity(intent);
                    requireActivity().finish();
                }
                else{
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_loginFragment);
                }


            }
        });

    }
}