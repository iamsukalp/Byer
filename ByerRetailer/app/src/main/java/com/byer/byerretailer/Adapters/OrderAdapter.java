package com.byer.byerretailer.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.InProgressOrderActivity;
import com.byer.byerretailer.Models.OrderModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.NewOrderListItemBinding;
import com.byer.byerretailer.databinding.OrderListItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;



import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.byer.byerretailer.ByerRetailer.productsRef;
import static com.byer.byerretailer.ByerRetailer.retailerRef;
import static com.byer.byerretailer.ByerRetailer.statusRef;
import static com.byer.byerretailer.ByerRetailer.userRef;
import static com.byer.byerretailer.Utils.Constants.ORDER_ACCEPTED_IMAGE;
import static com.byer.byerretailer.Utils.Constants.retailerId;


public class OrderAdapter extends RecyclerView.Adapter {
    Activity activity;
    String orderChild;
    List<OrderModel> orderList;
    List<String> orderKeyList;
    ProgressDialog progressDialog;
    private static final String ORDER_ID_TAG="orderId";


    public OrderAdapter(Activity requireActivity, String orderChild, List<String> orderKeyList, List<OrderModel> orderList) {
        this.activity=requireActivity;
        this.orderChild=orderChild;
        this.orderList=orderList;
        this.orderKeyList=orderKeyList;
        progressDialog=new ProgressDialog(activity);

        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Accepting Order");
    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(orderChild,"new")){
            return 0; // 0 for new order list item
        }
        else{
            return 1; // 1 for order list item
        }
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        if (viewType==0){
            return new NewOrderViewHolder(NewOrderListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else{
            return new OrderViewHolder(OrderListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        OrderModel model=orderList.get(position);
        if (TextUtils.equals(orderChild,"new")){
           //bind new order view holder
            NewOrderViewHolder newOrderViewHolder= (NewOrderViewHolder) holder;
            newOrderViewHolder.binding.newOrderUserName.setText(model.getUserName());
            newOrderViewHolder.binding.newOrderId.setText("Order No. "+orderKeyList.get(position));
            newOrderViewHolder.binding.newOrderAmount.setText("Amount: "+activity.getResources().getString(R.string.Rs)+model.getAmount());
            if (TextUtils.equals(model.getPaymentMode(),"UPI")){
                newOrderViewHolder.binding.newOrderPaymentMode.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_gpay));
                newOrderViewHolder.binding.newOrderPaymentStatus.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_completed_order));
            }
            else{
                newOrderViewHolder.binding.newOrderPaymentMode.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_cash));
                newOrderViewHolder.binding.newOrderPaymentStatus.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_close_color));
            }

            if (TextUtils.equals(model.getDeliveryMode(),"Pick Up")){
                    newOrderViewHolder.binding.newOrderDeliveryMode.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_pickup));
            }
            else{
                newOrderViewHolder.binding.newOrderDeliveryMode.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_home_delivery));
            }

            newOrderViewHolder.binding.newOrderAcceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    Map<String,Object> orderMap=new HashMap<>();
                    orderMap.put("status","in progress");

                    retailerRef.child(Constants.category).child(retailerId).child("Orders").child(orderKeyList.get(position)).updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    userRef.child(model.getUserId()).child("Orders").child(orderKeyList.get(position)).updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                notifyDataSetChanged();
                                                addItems(orderKeyList.get(position),model.getRetailerId(),model.getUserId(),model.getCategory());
                                            }
                                        }
                                    });

                                }
                                else{
                                    Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                        }
                    });


                }
            });

            newOrderViewHolder.binding.newOrderDeclineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    Map<String,Object> orderMap=new HashMap<>();
                    orderMap.put("status","decline");
                    retailerRef.child(Constants.category).child(retailerId).child("Orders").child(orderKeyList.get(position)).updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                           if (task.isSuccessful()){
                               retailerRef.child(Constants.category).child(retailerId).child("Orders").child(orderKeyList.get(position)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull @NotNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           progressDialog.dismiss();
                                       }
                                       else{
                                           Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                           progressDialog.dismiss();
                                       }

                                   }
                               });
                           }
                           else{
                               Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                               progressDialog.dismiss();
                           }
                        }
                    });

                }
            });


        }
        else{
         // bind order view holder
            OrderViewHolder orderViewHolder= (OrderViewHolder) holder;
            orderViewHolder.binding.orderAmount.setText("Amount: "+activity.getResources().getString(R.string.Rs)+model.getAmount());
            orderViewHolder.binding.orderId.setText("Order No. "+orderKeyList.get(position));
            orderViewHolder.binding.orderUserName.setText(model.getUserName());
            Date myDate = new Date(model.getTimestamp());
            DateFormat destDf = new SimpleDateFormat("MMM dd, yyyy  hh:mm");

            orderViewHolder.binding.orderTime.setText(destDf.format(myDate));

            orderViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (orderChild=="in progress"){
                        Intent intent=new Intent(activity, InProgressOrderActivity.class);
                        intent.putExtra(ORDER_ID_TAG,orderKeyList.get(position));
                        activity.startActivity(intent);
                    }
                }
            });


        }
    }

    private void addItems(String s, String retailerId, String userId, String category) {
        retailerRef.child(Constants.category).child(retailerId).child("Orders").child(s).child("Items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key=dataSnapshot.getKey();
                        String count= dataSnapshot.child("count").getValue().toString();

                   modifyItemCount(retailerId,s,userId,key,count);
                } }

                clearUserCart(s,userId,category);



            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void modifyItemCount(String userId, String s, String retailerId, String key, String count) {

        productsRef.child(retailerId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Map<String,Object> soldMap=new HashMap<>();
                    String soldCount=null;
                    int sc=0;
                    if (snapshot.hasChild("soldCount")){
                        sc= Integer.parseInt(snapshot.child("soldCount").getValue().toString());
                    }

                    soldCount= String.valueOf(sc+Integer.parseInt(count));

                    soldMap.put("soldCount",soldCount);

                    productsRef.child(retailerId).child(key).updateChildren(soldMap);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });




    }

    // clear user's cart after order is accepted
    private void clearUserCart(String s, String userId, String category) {

        userRef.child(userId).child("Cart").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){
                        updateOrderStatus(s,category);
                    }
                    else{
                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
            }
        });

    }
    // update order status
    private void updateOrderStatus(String s, String category) {

        Map<String,Object> statusMap=new HashMap<>();
        statusMap.put("status","Order Accepted");
        statusMap.put("timestamp",ServerValue.TIMESTAMP);
        statusMap.put("image",ORDER_ACCEPTED_IMAGE);

        retailerRef.child(Constants.category).child(retailerId).child("Orders").child(s).child("Status").child("0").setValue(statusMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){

                        statusRef.child(category).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String key=dataSnapshot.getKey();
                                    String title=dataSnapshot.child("title").getValue().toString();
                                    String image=dataSnapshot.child("image").getValue().toString();

                                    Map<String,Object> map=new HashMap<>();
                                    map.put("status",title);
                                    map.put("image",image);

                                    retailerRef.child(Constants.category).child(retailerId).child("Orders").child(s).child("Status").child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                addRetailerStatus(s,category);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });


                    }
                    else{
                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
            }
        });
    }


    // add status types to order based on the category
    private void addRetailerStatus(String s, String category) {
       statusRef.child(category).orderByChild("party").equalTo("Retailer").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

               for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                   String key=dataSnapshot.getKey();
                   String title=dataSnapshot.child("title").getValue().toString();

                   long viewType= (long) dataSnapshot.child("viewType").getValue();
                   long serial = (long) dataSnapshot.child("serial").getValue();
                   final HashMap<String, Object> retailerStatusMap = new HashMap<>();
                   retailerStatusMap.put("title", title);
                   retailerStatusMap.put("viewType", viewType);
                   retailerStatusMap.put("message",dataSnapshot.child("message").getValue().toString());
                   retailerStatusMap.put("image",dataSnapshot.child("image").getValue().toString());
                   retailerStatusMap.put("serial",serial);
                   retailerRef.child(Constants.category).child(retailerId).child("Orders").child(s).child("RetailerStatus").child(key).setValue(retailerStatusMap);
               }
               Toast.makeText(activity, "Order Accepted", Toast.LENGTH_SHORT).show();
               progressDialog.dismiss();
           }

           @Override
           public void onCancelled(@NonNull @NotNull DatabaseError error) {

           }
       });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }


    public static class NewOrderViewHolder extends RecyclerView.ViewHolder {
        NewOrderListItemBinding binding;
        public NewOrderViewHolder(NewOrderListItemBinding nb) {
            super(nb.getRoot());
            binding=nb;
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder{

        OrderListItemBinding binding;

        public OrderViewHolder(OrderListItemBinding ob) {
            super(ob.getRoot());
            binding=ob;
        }
    }
}
