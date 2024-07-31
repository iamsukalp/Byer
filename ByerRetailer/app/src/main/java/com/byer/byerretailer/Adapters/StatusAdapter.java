package com.byer.byerretailer.Adapters;

import android.animation.Animator;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.HomeActivity;
import com.byer.byerretailer.Activities.InProgressOrderActivity;
import com.byer.byerretailer.Models.StatusModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.Utils.FCMNotificationSender;
import com.byer.byerretailer.databinding.StatusListItemBtnTypeBinding;
import com.byer.byerretailer.databinding.StatusListItemEtTypeBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.byer.byerretailer.Activities.InProgressOrderActivity.retailerId;
import static com.byer.byerretailer.Activities.InProgressOrderActivity.shopLat;
import static com.byer.byerretailer.Activities.InProgressOrderActivity.shopLong;
import static com.byer.byerretailer.Activities.InProgressOrderActivity.userId;
import static com.byer.byerretailer.ByerRetailer.geoRef;
import static com.byer.byerretailer.ByerRetailer.partnerRef;
import static com.byer.byerretailer.ByerRetailer.retailerRef;

import com.google.firebase.messaging.RemoteMessage;


public class StatusAdapter extends RecyclerView.Adapter {
    List<StatusModel> statusList;
    List<String> statusListKey;
    Activity activity;
    String orderId;
    List<String> partnerKey=new ArrayList<>();
    List<String> partnerToken=new ArrayList<>();

    public StatusAdapter(InProgressOrderActivity inProgressOrderActivity, List<StatusModel> statusList, String orderId, List<String> statusListKey) {
        this.activity=inProgressOrderActivity;
        this.statusList=statusList;
        this.orderId=orderId;
        this.statusListKey=statusListKey;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType==0){
            return new ButtonTypeViewHolder(StatusListItemBtnTypeBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else{
            return new EditTextTypeViewHolder(StatusListItemEtTypeBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        StatusModel model=statusList.get(position);
        int viewType= (int) model.getViewType();

        if (viewType==0){
            return 0; // 0 for button list item
        }
        else{
            return 1; // 1 for ET list item
        }

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        StatusModel model=statusList.get(position);
        Log.d("POSITION",position+"");
        int viewType= (int) model.getViewType();
        if (viewType==0){
            ButtonTypeViewHolder btnViewHolder= (ButtonTypeViewHolder) holder;

            btnViewHolder.binding.statusListItemTitle.setText(model.getTitle());
            btnViewHolder.binding.statusListItemMessage.setText(model.getMessage());

            if (position!=0){
                btnViewHolder.binding.statusListItemDoneBtn.setEnabled(false);
                btnViewHolder.binding.statusListItemDoneBtn.setBackgroundColor(activity.getResources().getColor(R.color.grey,null));
            }

            btnViewHolder.binding.statusListItemDoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Map<String,Object> map=new HashMap<>();
                    map.put("status",model.getTitle());
                    map.put("timestamp", ServerValue.TIMESTAMP);
                    map.put("image",model.getImage());

                    retailerRef.child(HomeActivity.category).child(InProgressOrderActivity.retailerId).child("Orders").child(orderId).child("Status").child(statusListKey.get(position)).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            btnViewHolder.binding.statusListItemViewSwitcher.setDisplayedChild(1);
                            String key= statusListKey.get(position);
                            btnViewHolder.binding.statusListItemCompletedAnim.addAnimatorListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    retailerRef.child(Constants.category).child(Constants.retailerId).child("Orders").child(orderId).child("RetailerStatus").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (model.getSerial()==2){
                                                    assignDeliveryPartner();
                                                }
                                                notifyItemRemoved(position);

                                            }

                                        }
                                    });

                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                        }
                    });

                }
            });

        }
        else{
            EditTextTypeViewHolder etViewHolder= (EditTextTypeViewHolder) holder;
            etViewHolder.binding.statusListItemTitle.setText(model.getTitle());
            etViewHolder.binding.statusListItemMessage.setText(model.getMessage());

            if (position!=0){
                etViewHolder.binding.statusListItemDoneBtn.setEnabled(false);
                etViewHolder.binding.statusListItemDoneBtn.setBackgroundColor(activity.getResources().getColor(R.color.grey));
            }


        }


    }

    private void assignDeliveryPartner() {
        GeoFire geoFire;

        partnerKey.clear();
        partnerToken.clear();
        geoFire = new GeoFire(geoRef.child("Partners"));
        geoFire.queryAtLocation(new GeoLocation(shopLat,shopLong), 3).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if (key!=null){

                    partnerRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String partnerTokenKey=snapshot.child("token").getValue().toString();
                            Log.d("token", "onDataChange: "+partnerTokenKey);

                            if (snapshot.child("active").exists()){
                                int active= ((Long) snapshot.child("active").getValue()).intValue();

                                if (active<5){
                                    partnerKey.add(key);
                                    partnerToken.add(partnerTokenKey);
                                }
                            }
                            else{
                                partnerKey.add(key);
                                partnerToken.add(partnerTokenKey);
                            }
                            addOrder();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }



            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });



    }

    private void addOrder() {

        for (int i=0;i<partnerKey.size();i++){
            int finalI = i;
            retailerRef.child(Constants.category).child(retailerId).child("Orders").child(orderId).child("Delivery Requests").child(partnerKey.get(i)).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){
                        partnerRef.child(partnerKey.get(finalI)).child("Delivery Request").push().child("OrderId").setValue(orderId).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d("FCM token", "onComplete: "+partnerToken.get(finalI));
                                    FCMNotificationSender notificationSender=new FCMNotificationSender(partnerToken.get(finalI),
                                            "Delivery Request","New delivery request received tap to open",activity,activity);

                                    notificationSender.SendNotifications();
                                }    else{
                                    Toast.makeText(activity, "Couldn't send delivery request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(activity, "Couldn't send delivery request", Toast.LENGTH_SHORT).show();
                    }



                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public static class ButtonTypeViewHolder extends RecyclerView.ViewHolder{
        StatusListItemBtnTypeBinding binding;

        public ButtonTypeViewHolder(  StatusListItemBtnTypeBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }

    public static class EditTextTypeViewHolder extends RecyclerView.ViewHolder{
        StatusListItemEtTypeBinding binding;

        public EditTextTypeViewHolder(StatusListItemEtTypeBinding etb) {
            super(etb.getRoot());
            binding=etb;
        }
    }

}
