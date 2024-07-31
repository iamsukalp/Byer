package com.byer.byerretailer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.DialogChangeShopAvailabilityBinding;

public class ChangeShopAvailabilityDialog extends Dialog {

    private DialogChangeShopAvailabilityBinding binding;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean isShopOpen=false;
    private static final String SHOP_PREF_TAG= "isShopOpen";
    private static final String PREF_TAG= "MyPref";




    public ChangeShopAvailabilityDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogChangeShopAvailabilityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        binding.dialogShopAvailabilityConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShopAvailability();
                dismiss();
            }
        });

        binding.dialogShopAvailabilityCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    private void toggleShopAvailability() {
        editor.putBoolean(SHOP_PREF_TAG, !isShopOpen);
        editor.commit();

    }

    private void init() {
                pref=getContext().getSharedPreferences(PREF_TAG,0);
                editor=pref.edit();

                isShopOpen=pref.getBoolean(SHOP_PREF_TAG,false);
        Log.d("InsideDialog",isShopOpen+"");
                if (isShopOpen){
                        binding.dialogShopAvailabilityImage.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.closed));
                        binding.dialogShopAvailabilityLabel.setText("Do you want to close the shop?");
                        binding.dialogShopAvailabilityConfirm.setText("Close Shop");

                }
                else{
                    binding.dialogShopAvailabilityImage.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.open));
                    binding.dialogShopAvailabilityLabel.setText("Do you want to open the shop?");
                    binding.dialogShopAvailabilityConfirm.setText("Open Shop");
                }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setBackgroundDrawableResource(R.drawable.change_shop_availability_dialog_bg);
        init();
    }
}
