package com.byer.byer.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byer.Activities.TrackOrderActivity;
import com.byer.byer.Models.OrderModel;
import com.byer.byer.R;
import com.byer.byer.databinding.FragmentOrderBinding;
import com.byer.byer.databinding.OrderListItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.byer.byer.Byer.userRef;


public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private Query query;
    private FirebaseRecyclerOptions<OrderModel> options;
    private FirebaseRecyclerAdapter<OrderModel, OrderViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private int color;
    private static final String ORDER_ID_TAG="orderId";
    private static final String RETAILER_ID_TAG="retailerId";
    private static final String CATEGORY_ID_TAG="category";
    private String userId;
    private FirebaseAuth mAuth;


    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentOrderBinding.inflate(inflater,container,false);

        init();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrders();
    }

    private void getOrders() {
        options=new FirebaseRecyclerOptions.Builder<OrderModel>().setQuery(query,OrderModel.class).build();
        adapter=new FirebaseRecyclerAdapter<OrderModel, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull OrderViewHolder holder, int position, @NonNull @NotNull OrderModel model) {


                if (TextUtils.equals(model.getStatus(),"new")){
                    holder.binding.orderStatus.setText("Order placed");
                    color=requireActivity().getColor(R.color.yellow);
                }
                if (TextUtils.equals(model.getStatus(),"in progress")){
                    holder.binding.orderStatus.setText("In progress");
                    color=requireActivity().getColor(R.color.logo_color);
                }
                if (TextUtils.equals(model.getStatus(),"completed")){
                    holder.binding.orderStatus.setText("Delivered");
                    color=requireActivity().getColor(R.color.light_blue_600);
                }
                if (TextUtils.equals(model.getStatus(),"decline")){
                    holder.binding.orderStatus.setText("Declined");
                    color=requireActivity().getColor(R.color.payment_failed_bg);

                }
                holder.binding.orderStatus.setBackgroundColor(color);

           //     setStatusDrawableColor(holder.binding.orderStatus,color);


                if (TextUtils.equals(model.getPaymentMode(),"UPI")){
                    holder.binding.orderPaymentMode.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_gpay));
                    holder.binding.orderPaymentStatus.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_completed_order));
                }
                else{
                    holder.binding.orderPaymentMode.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_cash));
                    holder.binding.orderPaymentStatus.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_close_color));
                }

                if (TextUtils.equals(model.getDeliveryMode(),"Pick Up")){
                    holder.binding.orderDeliveryMode.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_pickup_16));
                }
                else{
                    holder.binding.orderDeliveryMode.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delivery_color_16));
                }


                holder.binding.orderRetailerName.setText(model.getShopName());
                holder.binding.orderAmount.setText("Amount: "+requireActivity().getResources().getString(R.string.Rs)+model.getAmount());
                holder.binding.orderId.setText("Order No. "+adapter.getRef(position).getKey());

                Date myDate = new Date(model.getTimestamp());
                DateFormat destDf = new SimpleDateFormat("hh:mm, MMM dd yyyy");

                holder.binding.orderTime.setText(destDf.format(myDate));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(requireActivity(), TrackOrderActivity.class);
                        intent.putExtra(ORDER_ID_TAG,adapter.getRef(position).getKey());
                        intent.putExtra(RETAILER_ID_TAG,model.getRetailerId());
                        intent.putExtra(CATEGORY_ID_TAG,model.getCategory());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new OrderViewHolder(OrderListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.orderList.setAdapter(adapter);
    }


    private void init() {
        // firebase auth
        mAuth= FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();


        // setting up recycler list
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();

        binding.orderList.setHasFixedSize(true);
        binding.orderList.setLayoutManager(linearLayoutManager);




        query=userRef.child(userId).child("Orders").orderByChild("timestamp");
    }


  /*  private void setStatusDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }*/

    private static class OrderViewHolder extends RecyclerView.ViewHolder {
        OrderListItemBinding binding;
        public OrderViewHolder(OrderListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}