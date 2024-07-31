package com.byer.byerdeliverypartner.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.byer.byerdeliverypartner.Activities.HomeActivity;
import com.byer.byerdeliverypartner.Utils.Constants;
import com.byer.byerdeliverypartner.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.byer.byerdeliverypartner.ByerPartner.partnerRef;


public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private String phone;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String mVerificationId;
    private boolean isCodeSent=false;
    private String token;
    private ProgressDialog progressDialog;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentLoginBinding.inflate(inflater,container,false);

        init();

        return binding.getRoot();
    }

    private void init() {
        // progress dialog definition
        progressDialog=new ProgressDialog(requireActivity());
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate()){
                    if (isCodeSent){

                        String code=binding.loginOtp.getText().toString().trim();
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
        binding.loginResendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(forceResendingToken);
            }
        });

        mCallback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signUpWithPhoneAuthCredential(phoneAuthCredential);
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
                binding.loginOtpContainer.setVisibility(View.VISIBLE);
                binding.loginOtpContainer.setEnabled(true);
                binding.loginResendOtpBtn.setEnabled(false);
            }

        };

    }

    private void signUpWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        Constants.mAuth.signInWithCredential(phoneAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // successfully signedIn, send the user to details page
                progressDialog.dismiss();

                // getting user auth token
                FirebaseUser user=authResult.getUser();
                assert user != null;
                String userId=user.getUid();
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull @NotNull Task<String> task) {
                        if (task.isSuccessful()){
                            Map<String,String> map=new HashMap<>();
                            map.put("token",task.getResult());
                            map.put("phone",phone);

                            partnerRef.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //sending user to details page
                                        if (Constants.partnerName!=null){
                                            Intent intent=new Intent(requireActivity(), HomeActivity.class);
                                            startActivity(intent);
                                            requireActivity().finish();
                                        }
                                        else{
                                            LoginFragmentDirections.ActionLoginFragmentToUserDetailsFragment action = LoginFragmentDirections.actionLoginFragmentToUserDetailsFragment();
                                            action.setPhone(phone);
                                            action.setToken(token);
                                            Navigation.findNavController(binding.getRoot()).navigate(action);
                                        }

                                    }
                                    else {
                                        Log.d("Login", "onComplete: "+task.getException().getMessage());
                                        Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                        else{
                            Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("Login", "onFailure: "+e.getMessage());
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        progressDialog.setMessage("Resending OTP");
        progressDialog.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(Constants.mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setForceResendingToken(forceResendingToken)
                        .setCallbacks(mCallback)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void startPhoneNumberVerification() {
        progressDialog.setMessage("Verifying Phone Number");
        progressDialog.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(Constants.mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(mCallback)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        isCodeSent=true;
        binding.loginLoginBtn.setText("Verify");
        binding.loginResendOtpBtn.setVisibility(View.VISIBLE);
        binding.loginResendOtpBtn.setEnabled(true);
    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential= PhoneAuthProvider.getCredential(mVerificationId,code);
        signUpWithPhoneAuthCredential(credential);
    }

    private boolean Validate() {
        if (TextUtils.isEmpty(binding.loginPhone.getText().toString())){
            return false;
        }
        else{
            phone="+91"+binding.loginPhone.getText().toString().trim();
            return true;
        }
    }
}