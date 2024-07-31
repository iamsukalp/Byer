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

import com.byer.byer.R;
import com.byer.byer.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private String phone;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String mVerificationId;
    private boolean isCodeSent=false;
    private String token;

    private ProgressDialog progressDialog;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);

        init();

        return binding.getRoot();



    }

    private void init() {

        // firebase definitions
        mAuth=FirebaseAuth.getInstance();

        // progress dialog definition
        progressDialog=new ProgressDialog(requireActivity());
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //UI Elements
        binding.signupOtpContainer.setVisibility(View.INVISIBLE);
        binding.signupOtpContainer.setEnabled(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.signupLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_signUpFragment_to_loginFragment);
            }
        });



        binding.signupContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate()){
                    if (isCodeSent){

                        String code=binding.signupOtp.getText().toString().trim();
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
        binding.signupOtpResendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(forceResendingToken);
            }
        });



        mCallback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signUpWithPhoneAuthCredential(phoneAuthCredential);
                //Getting the code sent by SMS
                String otp = phoneAuthCredential.getSmsCode();

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (otp != null) {
                    binding.signupOtp.setText(otp);
                    //verifying the code

                }
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
                binding.signupOtpContainer.setVisibility(View.VISIBLE);
                binding.signupOtpContainer.setEnabled(true);
                binding.signupOtpResendBtn.setEnabled(false);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(getActivity(), "Couldn't Retrieve Code!", Toast.LENGTH_SHORT).show();
                binding.signupOtpResendBtn.setEnabled(true);
            }
        };



    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential= PhoneAuthProvider.getCredential(mVerificationId,code);
        signUpWithPhoneAuthCredential(credential);
    }

    private void signUpWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // successfully signedIn, send the user to details page

                Toast.makeText(requireActivity(), "Working", Toast.LENGTH_SHORT).show();
                // getting user auth token
                FirebaseUser user=authResult.getUser();
                user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()){
                            token=task.getResult().getToken();
                            progressDialog.dismiss();
                            //sending user to details page
                            SignUpFragmentDirections.ActionSignUpFragmentToUserDetailsFragment action=SignUpFragmentDirections.actionSignUpFragmentToUserDetailsFragment();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        progressDialog.setMessage("Resending OTP");
        progressDialog.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setForceResendingToken(token)
                        .setCallbacks(mCallback)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

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
        binding.signupContinueBtn.setText("Verify");
        binding.signupOtpResendBtn.setVisibility(View.VISIBLE);

    }

    private boolean Validate() {
        boolean isValid=true;
        if (TextUtils.isEmpty(binding.signupPhone.getText().toString()) || binding.signupPhone.getText().toString().trim().length()<10){
            isValid=false;
        }
        else{
            phone="+91"+binding.signupPhone.getText().toString().trim();
        }
        return isValid;
    }
}