package com.byer.byerretailer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.byer.byerretailer.ByerRetailer;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.DialogReportCatalogueItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReportCatalogueItemDialog extends Dialog {


    private String iteName;
    private String date;
    private DialogReportCatalogueItemBinding binding;


    public ReportCatalogueItemDialog(@NonNull Context context, String itemName) {
        super(context);
        this.iteName=itemName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DialogReportCatalogueItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.DialogReportItemCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        binding.DialogReportItemReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();
            }
        });
    }

    private void sendReport() {

        String itemName=binding.DialogReportItemName.getText().toString().trim();
        String date=getDate();
        binding.DialogReportItemReportBtn.setEnabled(false);
        Map<String,Object> map=new HashMap<>();
        map.put("name",itemName);
        map.put("solved",false);
        map.put("date",getDate());
        map.put("seen",false);

        DatabaseReference notification_push = ByerRetailer.catalogueItemRequestRef.child(Constants.retailerId).push();
        final String push_id = notification_push.getKey();

        ByerRetailer.catalogueItemRequestRef.child(Constants.retailerId).child(push_id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Map<String,Object> map=new HashMap<>();
                            map.put("name",itemName);
                            map.put("solved",false);
                            map.put("date",date);
                            map.put("raisedBy",Constants.retailerId);


                            ByerRetailer.catalogueItemRequestRef.child("All").child(push_id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getContext(), "Request sent to Byer", Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    }
                                    else{
                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    }
                                }
                            });


                        }
                        else{
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.change_shop_availability_dialog_bg));
        init();
    }

    private void init() {
            binding.DialogReportItemName.setText(iteName);
    }
    private String getDate() {
        Calendar c = Calendar.getInstance();


        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        date = year + "-" + (month+1) + "-" + day;

        return date;
    }

}
