package com.byer.byer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byer.Models.ItemModel;
import com.byer.byer.Models.StatusModel;
import com.byer.byer.R;
import com.byer.byer.Utilities.Constants;
import com.byer.byer.databinding.ActivityTrackOrderBinding;
import com.byer.byer.databinding.OrderStatusListItemBinding;
import com.byer.byer.databinding.TrackOrderItemListItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static com.byer.byer.Byer.retailerRef;
import static com.byer.byer.Byer.userRef;

public class TrackOrderActivity extends AppCompatActivity {
    private static final String TAG=TrackOrderActivity.class.getSimpleName();
    private static final String ORDER_ID_TAG="orderId";
    private static final String RETAILER_ID_TAG="retailerId";
    private static final String CATEGORY_ID_TAG="category";
    private ActivityTrackOrderBinding binding;
    private FirebaseRecyclerOptions<StatusModel> options;
    private FirebaseRecyclerAdapter<StatusModel,StatusViewHolder> adapter;
    private Query query,itemQuery;
    private FirebaseRecyclerAdapter<ItemModel,ItemViewHolder> itemAdapter;
    private FirebaseRecyclerOptions<ItemModel> itemOptions;
    private LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
    private LinearLayoutManager itemLinearLayoutManager=new LinearLayoutManager(this);
    private List<Integer> pendingKeyList=new ArrayList<>();
    private List<Integer> completedKeyList=new ArrayList<>();
    private ColorMatrixColorFilter filter;
    private String orderId;
    private String retailerId;
    private String category;
    private String amount,retailerName;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityTrackOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();


        binding.TrackOrderItemListCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.scrollview.smoothScrollTo(0,binding.TrackOrderItemList.getTop());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getOrderStatusCompletedStatus();
        getItemList();
        getOrderDetails();
    }

    private void getOrderDetails() {
            userRef.child(userId).child("Orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    retailerName=snapshot.child("shopName").getValue().toString();
                    amount=snapshot.child("amount").getValue().toString();


                    binding.TrackOrderShopName.setText(retailerName);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
    }

    private void getItemList() {

        itemOptions=new FirebaseRecyclerOptions.Builder<ItemModel>().setQuery(itemQuery,ItemModel.class).build();
        itemAdapter=new FirebaseRecyclerAdapter<ItemModel, ItemViewHolder>(itemOptions) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ItemViewHolder holder, int position, @NonNull @NotNull ItemModel model) {
                holder.binding.trackorderItemName.setText(model.getName());
                holder.binding.trackorderItemCount.setText(model.getCount());
            }

            @NonNull
            @NotNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new ItemViewHolder(TrackOrderItemListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        itemAdapter.startListening();
        binding.TrackOrderItemList.setAdapter(itemAdapter);
    }

    private void getOrderStatusCompletedStatus() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                completedKeyList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.hasChild("timestamp")){
                        completedKeyList.add(Integer.valueOf(dataSnapshot.getKey()));
                        Log.d("Key", "onDataChange: "+dataSnapshot.getKey());
                    }

                }

                getOrderStatus(completedKeyList);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getOrderStatus(List<Integer> completedKeyList) {


        options=new FirebaseRecyclerOptions.Builder<StatusModel>().setQuery(query,StatusModel.class).build();
        adapter=new FirebaseRecyclerAdapter<StatusModel, StatusViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull StatusViewHolder holder, int position, @NonNull @NotNull StatusModel model) {

                if (position == 0) {
                    holder.binding.orderStatusCompletedUpperLine.setVisibility(View.INVISIBLE);
                }
                if (position==(adapter.getItemCount()-1)){
                    holder.binding.orderStatusCompletedLowerLine.setVisibility(View.INVISIBLE);
                }

                if (completedKeyList.get(completedKeyList.size()-1)>position){
                    holder.binding.orderStatusCompletedLowerLine.setVisibility(View.VISIBLE);
                }
                if (position== completedKeyList.size()-1){
                    holder.binding.orderStatusCompletedLowerLine.setVisibility(View.INVISIBLE);
                    holder.binding.orderStatusCompletedCircle.setVisibility(View.INVISIBLE);
                    holder.binding.orderStatusInprogressAnim.setVisibility(View.VISIBLE);
                }


                if (model.getTimestamp()!=0){

                    holder.binding.orderStatusTime.setVisibility(View.VISIBLE);
                    holder.binding.orderStatusTime.setText(model.getTimestamp()+"");
                    holder.binding.orderStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    holder.binding.orderStatusTitle.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.binding.orderStatusTitle.setTextColor(ContextCompat.getColor(TrackOrderActivity.this,R.color.black));




                    Log.d("Complete", "onBindViewHolder: "+ TrackOrderActivity.this.completedKeyList.size());


                }
                else{
                    pendingKeyList.add(position);
                    holder.binding.orderStatusTime.setVisibility(GONE);
                    holder.binding.orderStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                    holder.binding.orderStatusTitle.setTextColor(ContextCompat.getColor(TrackOrderActivity.this,R.color.default_text_color));

                    holder.binding.orderStatusCompletedUpperLine.setVisibility(View.INVISIBLE);
                    holder.binding.orderStatusCompletedLowerLine.setVisibility(View.INVISIBLE);
                    holder.binding.orderStatusCompletedCircle.setImageDrawable(ContextCompat.getDrawable(TrackOrderActivity.this,R.drawable.order_status_incomplete_circle));
                    holder.binding.orderStatusImage.setAlpha((float) 0.5);
                    holder.binding.orderStatusImage.setColorFilter(filter);



                }


                Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(holder.binding.orderStatusImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder).into(holder.binding.orderStatusImage);
                    }
                });

                holder.binding.orderStatusTitle.setText(model.getStatus());
                Date myDate = new Date(model.getTimestamp());
                DateFormat destDf = new SimpleDateFormat("hh:mm, MMM dd yyyy");

                holder.binding.orderStatusTime.setText(destDf.format(myDate));

            }

            @NonNull
            @NotNull
            @Override
            public StatusViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new StatusViewHolder(OrderStatusListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
            @Override
            public void onDataChanged() {
                // do your thing
                if (getItemCount() == 0) {
                    binding.TrackOrderViewSwitcher.setDisplayedChild(1);
                } else {
                    binding.TrackOrderViewSwitcher.setDisplayedChild(0);
                }
            }
        };
        adapter.startListening();
        binding.TrackOrderOrderStatusList.setAdapter(adapter);
    }

    private void init() {

        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        
        
        // getting orderId from intent
        orderId=getIntent().getStringExtra(ORDER_ID_TAG);
        retailerId=getIntent().getStringExtra(RETAILER_ID_TAG);
        category=getIntent().getStringExtra(CATEGORY_ID_TAG);

        binding.TrackOrderOrderNo.setText("Order No.: "+orderId);



        linearLayoutManager.isAutoMeasureEnabled();
        itemLinearLayoutManager.isAutoMeasureEnabled();
        binding.TrackOrderOrderStatusList.setHasFixedSize(true);
        binding.TrackOrderItemList.setHasFixedSize(true);
        binding.TrackOrderItemList.setLayoutManager(itemLinearLayoutManager);
        binding.TrackOrderOrderStatusList.setLayoutManager(linearLayoutManager);

        // for saturating image
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        filter = new ColorMatrixColorFilter(matrix);


        Log.d("WORD", "init: "+category+" "+retailerId+" "+orderId);

        // status query
        query=retailerRef.child(category).child(retailerId).child("Orders").child(orderId).child("Status").orderByKey();

        //items query
        itemQuery=userRef.child(userId).child("Orders").child(orderId).child("Items");

    }


    private static class StatusViewHolder extends RecyclerView.ViewHolder {
        OrderStatusListItemBinding binding;
        public StatusViewHolder(OrderStatusListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        TrackOrderItemListItemBinding binding;

        public ItemViewHolder(TrackOrderItemListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}