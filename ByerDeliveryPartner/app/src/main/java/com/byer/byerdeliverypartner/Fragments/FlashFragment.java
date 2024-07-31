package com.byer.byerdeliverypartner.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byer.byerdeliverypartner.Activities.HomeActivity;
import com.byer.byerdeliverypartner.R;
import com.byer.byerdeliverypartner.Utils.Constants;
import com.byer.byerdeliverypartner.databinding.FragmentFlashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.byer.byerdeliverypartner.ByerPartner.partnerRef;

public class FlashFragment extends Fragment {

    private FragmentFlashBinding binding;
    private FirebaseAuth mAuth;


    public FlashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentFlashBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    private void init() {
        mAuth=FirebaseAuth.getInstance();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        },1500);



    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser()!=null) { // check if user is logged in

            // check if user is registered
            partnerRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // user is logged in and registered ==> send user to home Activity
                        Intent intent = new Intent(requireActivity(), HomeActivity.class);
                        startActivity(intent);
                        requireActivity().finish();

                    } else {// if user is logged in but not registered
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_userDetailsFragment);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        }
        else{ // if user is not logged in
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_loginFragment);
        }
    }
}