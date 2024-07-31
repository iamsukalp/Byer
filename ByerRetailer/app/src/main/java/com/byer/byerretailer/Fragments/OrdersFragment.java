package com.byer.byerretailer.Fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byer.byerretailer.Activities.HomeActivity;
import com.byer.byerretailer.Adapters.OrderAdapter;
import com.byer.byerretailer.Models.OrderModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.FragmentOrdersBinding;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.byer.byerretailer.ByerRetailer.retailerRef;


public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth mAuth;
    private String retailerId;
    private Query query;
    private String orderChild;
    private List<OrderModel> orderList=new ArrayList<>();
    private List<String> orderKeyList=new ArrayList<>();
    private OrderAdapter adapter;

    public OrdersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentOrdersBinding.inflate(inflater, container, false);

        init();
        return binding.getRoot();


    }


    private void init() {

        //firebase components
        mAuth= FirebaseAuth.getInstance();
        retailerId=mAuth.getCurrentUser().getUid();

        retailerRef.child(retailerId).child("Orders").keepSynced(true);


        binding.orderFilterChipGroup.check(R.id.order_new);
        binding.orderNew.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
        binding.orderNew.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.logo_color)));


        // setting up recyclerview
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();
        binding.orderList.setHasFixedSize(true);
        binding.orderList.setLayoutManager(linearLayoutManager);



    }

    @Override
    public void onStart() {
        super.onStart();
        orderChild="new";
        getOrders(orderChild);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.orderFilterChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                switch (checkedId){
                    case R.id.order_new:
                    {

                        binding.orderNew.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                        binding.orderInProgress.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));
                        binding.orderCompleted.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));

                        binding.orderNew.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.logo_color)));
                        binding.orderInProgress.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));
                        binding.orderCompleted.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));

                        getOrders("new");

                        break;
                    }
                    case R.id.order_inProgress:{

                        binding.orderNew.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));
                        binding.orderInProgress.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                        binding.orderCompleted.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));

                        binding.orderNew.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));
                        binding.orderInProgress.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.logo_color)));
                        binding.orderCompleted.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));

                        getOrders("in progress");
                        break;
                    }
                    case R.id.order_completed:{


                        binding.orderNew.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));
                        binding.orderInProgress.setTextColor(ContextCompat.getColor(getActivity(),R.color.black));
                        binding.orderCompleted.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));

                        binding.orderNew.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));
                        binding.orderInProgress.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.chip_grey)));
                        binding.orderCompleted.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.logo_color)));

                        getOrders("completed");
                        break;
                    }
                }


            }
        });



    }

    private void getOrders(String orderChild) {
        adapter=new OrderAdapter(requireActivity(),orderChild,orderKeyList,orderList);
        binding.orderList.setAdapter(adapter);
        orderList.clear();
        orderKeyList.clear();
        adapter.notifyDataSetChanged();

        query=retailerRef.child(HomeActivity.category).child(retailerId).child("Orders").orderByChild("status").equalTo(orderChild);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                if (snapshot.hasChildren()){
                    binding.orderViewSwitcher.setDisplayedChild(0);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel model = dataSnapshot.getValue(OrderModel.class);
                        orderList.add(model);
                        String key=dataSnapshot.getKey();
                        orderKeyList.add(key);
                    }
                    adapter.notifyDataSetChanged();
                }
                else{
                    adapter.notifyDataSetChanged();
                    binding.orderViewSwitcher.setDisplayedChild(1);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}