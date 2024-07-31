package com.flashotech.byerconsole.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.flashotech.byerconsole.Activities.CatalogueActivity;
import com.flashotech.byerconsole.Activities.HomeActivity;
import com.flashotech.byerconsole.ByerConsole;
import com.flashotech.byerconsole.Models.CategoryModel;
import com.flashotech.byerconsole.R;
import com.flashotech.byerconsole.databinding.CategoryItemBinding;
import com.flashotech.byerconsole.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static com.flashotech.byerconsole.ByerConsole.menuRef;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String CATEGORY_TAG= "category";
    private static final String SUB_CATEGORY_TAG= "sub-category";
    private FirebaseRecyclerOptions<CategoryModel> options;
    private FirebaseRecyclerAdapter<CategoryModel,CategoryViewHolder> adapter;
    private String key;
    private LinearLayoutManager linearLayoutManager;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {

        // recycler setup
        linearLayoutManager = new GridLayoutManager(requireActivity(),3);
        // menu recycler view properties
        binding.categoryList.setHasFixedSize(true);
        binding.categoryList.setLayoutManager(linearLayoutManager);

        getCategory();
    }

    private void getCategory() {
        Query query=menuRef;
        options=new FirebaseRecyclerOptions.Builder<CategoryModel>().setQuery(query,CategoryModel.class).build();
        adapter=new FirebaseRecyclerAdapter<CategoryModel, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull CategoryModel model) {
                Picasso.get().load(model.getIcon()).placeholder(R.drawable.ic_grocery).networkPolicy(NetworkPolicy.OFFLINE).into(holder.categoryBinding.gridCategoryImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getIcon()).placeholder(R.drawable.ic_grocery).into(holder.categoryBinding.gridCategoryImage);
                    }
                });

                holder.categoryBinding.gridCategoryLabel.setText(model.getName());
                holder.categoryBinding.gridCategoryAvailableSwitch.setChecked(model.isAvailable());
                holder.categoryBinding.gridCategoryAvailableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        key=adapter.getRef(position).getKey();
                        changeAvailability(key,isChecked,model.getName());
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(requireActivity(), CatalogueActivity.class);
                        intent.putExtra(CATEGORY_TAG,model.getCategory());
                        intent.putExtra(SUB_CATEGORY_TAG, R.array.Grocery_sub);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new CategoryViewHolder(CategoryItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.categoryList.setAdapter(adapter);
    }

    private void changeAvailability(String key, boolean isChecked, String name) {

        Map<String,Object> map=new HashMap<>();
        map.put("available",isChecked);
        menuRef.child(key).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(requireActivity(), name + " available:"+ isChecked, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(requireActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        CategoryItemBinding categoryBinding;
        public CategoryViewHolder(CategoryItemBinding b) {
            super(b.getRoot());
            categoryBinding=b;
        }
    }
}