package com.byer.byerretailer.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.byer.byerretailer.Activities.RegisterItemActivity;
import com.byer.byerretailer.Adapters.ProductListItemAdapter;
import com.byer.byerretailer.Helper.SwipeEventHandlerRecycler;
import com.byer.byerretailer.Models.ProductListModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.Utils.Constants;
import com.byer.byerretailer.databinding.FragmentHomeBinding;
import com.byer.byerretailer.databinding.ProductListItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private  static FragmentHomeBinding binding;
    private List<ProductListModel> productList;
    private List<String> productKeyList;
    private ProductListItemAdapter adapter;
    private DatabaseReference productRef;
    private Query query;
    private LinearLayoutManager linearLayoutManager;
    private String subcategory_filter;
    private String retailerId;
    private FirebaseAuth mAuth;


    public static boolean filterOpen=false;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static void closeFilter() {
        filterOpen=false;
        binding.homeTransformationLayout.finishTransform();
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(inflater, container, false);
        init();
        initializeListeners();
        return binding.getRoot();
    }

    private void initializeListeners() {
        binding.homeFilterItemBtn.setOnClickListener(this);
        binding.homeAddItemBtn.setOnClickListener(this);
        binding.homeCancelFilterBtn.setOnClickListener(this);

    }

    private void init() {
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        retailerId=mAuth.getCurrentUser().getUid();


        // recycler view setup
        binding.homeProductListRecycler.setHasFixedSize(true);
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.homeProductListRecycler.setLayoutManager(linearLayoutManager);
        binding.homeProductListRecycler.setItemAnimator(null);


        productList = new ArrayList<>();
        productKeyList=new ArrayList<>();
        adapter = new ProductListItemAdapter(requireActivity(), productList,productKeyList);
        binding.homeProductListRecycler.setAdapter(adapter);


        productRef= FirebaseDatabase.getInstance().getReference().child("Products");
        productRef.child(retailerId).keepSynced(true);

        query=productRef.child(retailerId).orderByChild("available");
        getResult(query);

        // attaching swipe event handler to recycler view
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeEventHandlerRecycler(adapter,requireActivity()));
        itemTouchHelper.attachToRecyclerView(binding.homeProductListRecycler);

    }


    private void getResult(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                productList.clear();
                productKeyList.clear();
                if (snapshot.exists()){
                    binding.homeViewSwitcher.setDisplayedChild(0);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.d("HOMEFRAGMENT", "onDataChange: "+dataSnapshot.getKey());
                        ProductListModel model = dataSnapshot.getValue(ProductListModel.class);
                        productList.add(model);
                        String key=dataSnapshot.getKey();
                        productKeyList.add(key);
                    }
                    adapter.notifyDataSetChanged();
                }
                else{
                    productList.clear();
                    adapter.notifyDataSetChanged();
                    binding.homeViewSwitcher.setDisplayedChild(1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }






    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        binding.homeProductListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.homeAddItemBtn.show();
                    binding.homeFilterItemBtn.show();
                } else {
                    binding.homeAddItemBtn.hide();
                    binding.homeFilterItemBtn.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        binding.homeFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0){
                    subcategory_filter= String.valueOf(parent.getItemAtPosition(position));
                    Log.d("Filter", subcategory_filter);
                    binding.homeTransformationLayout.finishTransform();
                    query=productRef.child(Constants.retailerId).orderByChild("subcategory").equalTo(subcategory_filter);
                    getResult(query);
                }
                else{
                    Toast.makeText(requireActivity(), "Select a category", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void presentActivity(View v) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(requireActivity(), v, "transition");
        int revealX = (int) (v.getX() + v.getWidth() / 2);
        int revealY = (int) (v.getY() + v.getHeight() / 2);

        Intent intent = new Intent(requireActivity(), RegisterItemActivity.class);
        intent.putExtra(RegisterItemActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(RegisterItemActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);


        ActivityCompat.startActivity(requireActivity(), intent, options.toBundle());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.home_addItemBtn:{
                presentActivity(v);
                break;
            }
            case R.id.home_filterItemBtn:{
                filterOpen=true;
                binding.homeTransformationLayout.startTransform();
                break;
            }
            case R.id.home_cancelFilterBtn:{
                filterOpen=false;
                binding.homeTransformationLayout.finishTransform();
                break;

            }



        }
    }

}