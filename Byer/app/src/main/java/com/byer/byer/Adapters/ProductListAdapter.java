package com.byer.byer.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byer.Activities.ProductListActivity;
import com.byer.byer.Models.ProductModel;
import com.byer.byer.R;
import com.byer.byer.databinding.ProductListItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.byer.byer.Byer.userRef;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> implements Filterable {
    Activity activity;
    private List<ProductModel> itemList;
    private List<ProductModel> itemListAll;
    private List<String> keyList;
    private String userId;
    String retailerId;
    String distance;
    String category;
    private FirebaseAuth mAuth;

    public ProductListAdapter(ProductListActivity productListActivity, List<String> keyList, List<ProductModel> itemList, List<ProductModel> itemListAll) {
            this.activity=productListActivity;
            this.keyList=keyList;
            this.itemList=itemList;
            this.itemListAll=itemListAll;

    }


    @NonNull
    @Override
    public ProductListAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(ProductListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductListAdapter.ProductViewHolder holder, int position) {
       ProductModel model=itemList.get(position);
       mAuth=FirebaseAuth.getInstance();
       userId=mAuth.getCurrentUser().getUid();
        retailerId=ProductListActivity.retailerId;
        distance=ProductListActivity.distance;
        category=ProductListActivity.category;


        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);



        userRef.child(userId).child("Cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String itemKey=keyList.get(position);

                if (snapshot.hasChild(itemKey)){
                    holder.binding.itemViewSwitcher.setDisplayedChild(1);
                    holder.binding.productListAdd.setEnabled(true);
                    holder.binding.productListSubstract.setEnabled(true);
                    holder.binding.productListCount.setText(snapshot.child(itemKey).child("count").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!model.isAvailable()){
            holder.binding.productListImage.setAlpha((float) 0.5);
            holder.binding.productListImage.setColorFilter(filter);

            holder.binding.productListName.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.grey_light,null)));
            holder.binding.productListQuantity.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.grey_light,null)));
            holder.binding.productListPrice.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.grey_light,null)));
        }
        else{
            holder.binding.productListImage.setAlpha((float) 1.0);
            holder.binding.productListImage.setColorFilter(null);

            holder.binding.productListName.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.black,null)));
            holder.binding.productListQuantity.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.black,null)));
            holder.binding.productListPrice.setTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.black,null)));
        }

        Picasso.get().load(model.getImage()).networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.binding.productListImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getImage())
                                .into(holder.binding.productListImage);
                    }
                });

        String quantity=model.getQuantity()+" "+model.getUnit();
        String sp=activity.getResources().getString(R.string.Rs)+" "+model.getSp();

        holder.binding.productListName.setText(model.getName());
        holder.binding.productListQuantity.setText(quantity);
        holder.binding.productListPrice.setText(sp);

        holder.binding.productListAddToCart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                addToCart(holder,keyList.get(position),model.getSp(),model.getQuantity(),model.getName(),model.getUnit());
                holder.binding.itemViewSwitcher.setDisplayedChild(1);
                holder.binding.productListAdd.setEnabled(false);
                holder.binding.productListSubstract.setEnabled(false);
                holder.binding.productListCount.setText("1");


            }
        });

        holder.binding.productListAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count;
                count= Integer.parseInt(holder.binding.productListCount.getText().toString());
                holder.binding.productListCount.setText((count+1)+"");
                holder.binding.productListAdd.setEnabled(false);
                holder.binding.productListSubstract.setEnabled(false);
                updateCart(holder,keyList.get(position),model.getSp(),(count+1));




            }
        });

        holder.binding.productListSubstract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count;
                holder.binding.productListAdd.setEnabled(false);
                holder.binding.productListSubstract.setEnabled(false);
                count= Integer.parseInt(holder.binding.productListCount.getText().toString());
                if (count==1){

                    removeCart(keyList.get(position),holder);
                }
                else{
                    holder.binding.productListCount.setText((count-1)+"");
                    updateCart(holder, keyList.get(position),model.getSp(),(count-1));
                }



            }
        });
    }

    private void removeCart(String key, ProductViewHolder holder) {
        userRef.child(userId).child("Cart").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    holder.binding.itemViewSwitcher.setDisplayedChild(0);
                    holder.binding.productListAddToCart.setEnabled(true);
                }
                else{
                    userRef.child(userId).child("Cart").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                holder.binding.itemViewSwitcher.setDisplayedChild(0);
                                holder.binding.productListAddToCart.setEnabled(true);

                            }
                            else{
                                Toast.makeText(activity, "Couldn't update the cart", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateCart(ProductViewHolder holder, String key, String sp, int i) {
        HashMap<String, Object> cartMap=new HashMap<>();
        cartMap.put("count",i+"");
        cartMap.put("price",(Integer.parseInt(sp)*i)+"");
        userRef.child(userId).child("Cart").child(key).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(activity, "Couldn't update cart", Toast.LENGTH_SHORT).show();
                }
                holder.binding.productListAdd.setEnabled(true);
                holder.binding.productListSubstract.setEnabled(true);
            }
        });
    }

    private void addToCart(ProductViewHolder holder, String key, String sp, String quantity, String name, String unit) {


        HashMap<String, Object> cartMap=new HashMap<>();
        cartMap.put("quantity",quantity);
        cartMap.put("retailerId",retailerId);
        cartMap.put("itemId",key);
        cartMap.put("unit",unit);
        cartMap.put("category",category);
        cartMap.put("distance",distance);
        cartMap.put("name",name);
        cartMap.put("count","1");
        cartMap.put("price",sp);



        userRef.child(userId).child("Cart").child(key).setValue(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(activity, "Couldn't add the item to the list", Toast.LENGTH_SHORT).show();
                }
                else{
                    holder.binding.productListAdd.setEnabled(true);
                    holder.binding.productListSubstract.setEnabled(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter=new Filter() {

        // runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<ProductModel> filteredList =new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(itemListAll);

            }
            else{



                for(ProductModel model:itemList){
                    String filterPattern=constraint.toString().toLowerCase();

                    if (model.getName().toLowerCase().contains(filterPattern)){
                        Log.d("Filter", "performFiltering: "+model.getName());
                        filteredList.add(model);

                    }
                }
            }

            FilterResults results=new FilterResults();
            results.values=filteredList;

            return results;
        }

        // runs on ui thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            itemList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ProductListItemBinding binding;

        public ProductViewHolder(ProductListItemBinding pb) {
            super(pb.getRoot());
            binding=pb;
        }
    }
}
