package com.byer.byer.Fragments;

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

import com.byer.byer.Activities.HomeActivity;
import com.byer.byer.R;
import com.byer.byer.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.byer.byer.Byer.userRef;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private String phone;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String mVerificationId;
    private boolean isCodeSent=false;
    private String token;
    private String email;

    private ProgressDialog progressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentLoginBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        // progress dialog definition
        progressDialog=new ProgressDialog(requireActivity());
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);



        // google OAuth definition
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.loginSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_loginFragment_to_signUpFragment);
            }
        });


        // phone authentication function
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

        // Google sign in function
        binding.loginGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
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
        mAuth.signInWithCredential(phoneAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // successfully signedIn, send the user to details page
                progressDialog.dismiss();

                // getting user auth token
                FirebaseUser user=authResult.getUser();
                String userId=user.getUid();
                assert user != null;
                user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()){
                            token= Objects.requireNonNull(task.getResult()).getToken();
                            //sending user to details page
                            userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
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

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

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

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            email=user.getEmail();
                            assert user != null;
                            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()){
                                        token= Objects.requireNonNull(task.getResult()).getToken();
                                        updateUI(user,token);
                                    }
                                    else{
                                        Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null, token);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user, String token) {
        if(user!=null){

            LoginFragmentDirections.ActionLoginFragmentToGooglePhoneInputFragment action = LoginFragmentDirections.actionLoginFragmentToGooglePhoneInputFragment();
            action.setToken(token);
            action.setEmail(email);
            Navigation.findNavController(binding.getRoot()).navigate(action);

        }
        else{
            Toast.makeText(requireActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

}