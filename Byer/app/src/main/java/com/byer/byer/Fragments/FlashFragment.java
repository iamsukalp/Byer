package com.byer.byer.Fragments;

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

import com.byer.byer.Activities.HomeActivity;
import com.byer.byer.R;
import com.byer.byer.Utilities.Constants;
import com.byer.byer.databinding.FragmentFlashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.byer.byer.Byer.userRef;


public class FlashFragment extends Fragment {

    private FragmentFlashBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;

    public FlashFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentFlashBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();

    }

    private void init() {

        mAuth= FirebaseAuth.getInstance();

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser()!=null){
                    Constants.getConstants();
                    userRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                                requireActivity().startActivity(intent);
                                requireActivity().finish();
                            }
                            else{
                                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_loginFragment);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
                else{
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_loginFragment);
                }
            }
        },1000);
       /* binding.flashAnim.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);



            }
        });
*/
    }
}