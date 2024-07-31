package com.byer.byer.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.byer.byer.databinding.FragmentGooglePhoneInputBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class GooglePhoneInputFragment extends Fragment {

    private FragmentGooglePhoneInputBinding binding;
    private FirebaseAuth mAuth;
    private String phone;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private String mVerificationId;
    private boolean isCodeSent=false;
    private String token;
    private String email;
    private ProgressDialog progressDialog;

    public GooglePhoneInputFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentGooglePhoneInputBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();

    }

    private void init() {
        //firebase Components
        mAuth=FirebaseAuth.getInstance();

        // progress dialog
        progressDialog=new ProgressDialog(requireActivity());
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // getting data from safeArgs
        assert getArguments() != null;
        token= GooglePhoneInputFragmentArgs.fromBundle(getArguments()).getToken();
        email=GooglePhoneInputFragmentArgs.fromBundle(getArguments()).getEmail();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.googlePhoneInputContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate()){

                    if (isCodeSent){

                        String code=binding.googlePhoneInputOtp.getText().toString().trim();
                        if (TextUtils.isEmpty(code)){
                            Toast.makeText(requireActivity(), "Please enter the  OTP", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            verifyPhoneNumberWithCode(mVerificationId,code);
                        }

                    }else{
                        startPhoneNumberVerification();
                    }
                }
            }
        });
        mCallback =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                linkPhoneWithGoogle(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);
                mVerificationId=s;
                forceResendingToken=token;
                progressDialog.dismiss();
            }
        };
    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential= PhoneAuthProvider.getCredential(mVerificationId,code);
        linkPhoneWithGoogle(credential);
    }

    private void startPhoneNumberVerification() {

        progressDialog.setMessage("Verifying Phone Number");
        progressDialog.show();
        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(mCallback)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        isCodeSent=true;
        binding.googlePhoneInputContinue.setText("Verify");
    }

    private void linkPhoneWithGoogle(PhoneAuthCredential authCredential) {
        Objects.requireNonNull(mAuth.getCurrentUser()).linkWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    // sending user to details page
                    GooglePhoneInputFragmentDirections.ActionGooglePhoneInputFragmentToGoogleUserDetailsInputFragment action= GooglePhoneInputFragmentDirections.actionGooglePhoneInputFragmentToGoogleUserDetailsInputFragment();
                    action.setEmail(email);
                    action.setPhone(phone);
                    action.setToken(token);
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(requireActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean Validate() {
        phone="+91-"+binding.googlePhoneInputPhone.getText().toString().trim();
        if (phone.length()<5){
            binding.googlePhoneInputPhone.setError("Please enter a valid phone number");
            return  false;
        }
        return true;
    }
}