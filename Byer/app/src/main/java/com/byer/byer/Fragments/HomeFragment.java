package com.byer.byer.Fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byer.Activities.RetailerListActivity;
import com.byer.byer.Models.MenuModel;
import com.byer.byer.R;
import com.byer.byer.databinding.FragmentHomeBinding;
import com.byer.byer.databinding.MenuGridItemBinding;

import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import org.jetbrains.annotations.NotNull;

import static com.byer.byer.Byer.menuRef;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseRecyclerOptions<MenuModel> options;
    private FirebaseRecyclerAdapter<MenuModel,MenuViewHolder> adapter;
    private Query query;
    private LinearLayoutManager linearLayoutManager;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(inflater,container,false);

        init();
        getMenu();
        getNotification();
        return binding.getRoot();


    }

    private void getNotification() {
        // get notifications here
    }

    private void init() {
        query= menuRef.orderByChild("available");

        // recycler setup
        linearLayoutManager = new GridLayoutManager(getContext(),3);


        // menu recycler view properties
        binding.homeMenuList.setHasFixedSize(true);
        binding.homeMenuList.setLayoutManager(linearLayoutManager);


    }

    private void getMenu() {
        options=new FirebaseRecyclerOptions.Builder<MenuModel>().setQuery(query,MenuModel.class).build();
        adapter=new FirebaseRecyclerAdapter<MenuModel, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull MenuModel model) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

                if (!model.isAvailable()){
                    holder.menuBinding.menuListImage.setAlpha((float) 0.7);
                    holder.menuBinding.menuListImage.setColorFilter(filter);
                    holder.menuBinding.menuListText.setTextColor(ColorStateList.valueOf(requireActivity().getColor(R.color.grey)));
                }
                else{
                    holder.menuBinding.menuListImage.setAlpha((float) 1.0);
                    holder.menuBinding.menuListImage.setColorFilter(null);
                    holder.menuBinding.menuListText.setTextColor(ColorStateList.valueOf(requireActivity().getColor(R.color.black)));
                }
                Picasso.get().load(model.getIcon()).placeholder(R.drawable.ic_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(holder.menuBinding.menuListImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getIcon()).placeholder(R.drawable.ic_image_placeholder).into(holder.menuBinding.menuListImage);
                    }
                });

                holder.menuBinding.menuListText.setText(model.getName());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(getActivity(), RetailerListActivity.class);
                        intent.putExtra("Category",model.getCategory());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MenuViewHolder(MenuGridItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        };
        adapter.startListening();
        binding.homeMenuList.setAdapter(adapter);

    }

    private static class MenuViewHolder extends RecyclerView.ViewHolder {
        MenuGridItemBinding menuBinding;

        public MenuViewHolder(MenuGridItemBinding b) {
            super(b.getRoot());
            menuBinding=b;
        }
    }
}