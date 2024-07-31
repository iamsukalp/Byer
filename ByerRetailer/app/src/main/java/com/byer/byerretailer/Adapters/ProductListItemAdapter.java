package com.byer.byerretailer.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.transition.TransitionManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.EditProductActivity;
import com.byer.byerretailer.Models.ProductListModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.ProductListItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductListItemAdapter extends RecyclerView.Adapter<ProductListItemAdapter.ProductListItemViewHolder> {
    private Context mCtx;
    private List<ProductListModel> productList;
    private List<String> productKeyList;
    private Resources res;
    private DatabaseReference productRef = FirebaseDatabase.getInstance().getReference().child("Products");
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private String current_user;
    private boolean isOpen=false;
    private ConstraintSet constraintSetShow= new ConstraintSet();
    private ConstraintSet constraintSetHide=new ConstraintSet();


    public ProductListItemAdapter(Context mCtx, List<ProductListModel> productList, List<String> productKeyList) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.productKeyList=productKeyList;
        current_user=mAuth.getCurrentUser().getUid();

    }
    @NonNull
    @Override
    public ProductListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductListItemViewHolder(ProductListItemBinding.inflate(LayoutInflater.from(mCtx),parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProductListItemViewHolder holder, int position) {

        constraintSetShow.clone(mCtx,R.layout.product_list_item_selected);
        constraintSetHide.clone(holder.binding.productListItemRoot);

        ProductListModel model=productList.get(position);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        res=mCtx.getResources();

        float mrp,sp;
        float discount;

        mrp= Integer.parseInt(model.getMrp());
        sp= Integer.parseInt(model.getSp());
        discount=((mrp-sp)/mrp)*100;

        int soldCount=0;
        if (!TextUtils.isEmpty(model.getSoldCount())){
            soldCount= Integer.parseInt(model.getSoldCount());
        }
        int totalAvailableCount= Integer.parseInt(model.getTotalAvailableQuantity());

        int soldPercentage= (int) Math.round(soldCount* 100.0/totalAvailableCount);

        holder.binding.productListItemQuantityAvailableSoldLabel.setText("Sold: "+soldCount);
        holder.binding.productListItemQuantityAvailableTotalLabel.setText("Total: "+totalAvailableCount);
       /* holder.binding.productListItemQuantityAvailable.setMax(totalAvailableCount);
        holder.binding.productListItemQuantityAvailable.setProgress(soldPercentage);*/


        if (soldPercentage>=95){  // mark item unavailable if sold percentage is >=95% to keep a buffer of 5%, in case of overlapping order placements
                   if (model.isAvailable()){
                       Map<String,Object> map=new HashMap<>();
                       map.put("available",false);
                       productRef.child(current_user).child(productKeyList.get(position)).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   notifyDataSetChanged();
                                   Toast.makeText(mCtx, "Item marked unavailable", Toast.LENGTH_SHORT).show();
                               }
                               else{
                                   Toast.makeText(mCtx, "Couldn't make the change", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                   }
        }



        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        Picasso.get().load(model.getImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_image_placeholder).into(holder.binding.productListItemImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder).into(holder.binding.productListItemImage);
            }
        });

        holder.binding.productListItemSubcategory.setText(model.getSubcategory());
        holder.binding.productListItemSp.setText("Selling Price: "+res.getString(R.string.Rs)+" "+model.getSp());
        if (TextUtils.equals(model.getQuantity(),"1")){
            holder.binding.productListItemMrp.setText("MRP: "+res.getString(R.string.Rs)+" "+model.getMrp()+res.getString(R.string.divider)+" "+model.getUnit());
        }else{
            holder.binding.productListItemMrp.setText("MRP: "+res.getString(R.string.Rs)+" "+model.getMrp()+res.getString(R.string.divider)+model.getQuantity()+" "+model.getUnit());
        }

        holder.binding.productListItemName.setText(model.getName());
        if (discount==0.0){
            holder.binding.productListItemDiscount.setVisibility(View.GONE);
        }
        else{
            holder.binding.productListItemDiscount.setVisibility(View.VISIBLE);
            holder.binding.productListItemDiscount.setText("-"+discount+"%");
        }


        if (!model.isAvailable()){
            holder.binding.productListItemImage.setAlpha((float) 0.5);
            holder.binding.productListItemImage.setColorFilter(filter);
            holder.binding.productListItemName.setTextColor(ColorStateList.valueOf(res.getColor(R.color.grey,null)));
            holder.binding.productListItemMrp.setTextColor(ColorStateList.valueOf(res.getColor(R.color.grey,null)));
            holder.binding.productListItemSp.setTextColor(ColorStateList.valueOf(res.getColor(R.color.grey,null)));
            holder.binding.productListItemSubcategory.setTextColor(ColorStateList.valueOf(res.getColor(R.color.grey,null)));
        }
        else{
            holder.binding.productListItemImage.setAlpha((float) 1.0);
            holder.binding.productListItemImage.setColorFilter(null);
            holder.binding.productListItemName.setTextColor(ColorStateList.valueOf(res.getColor(R.color.black,null)));
            holder.binding.productListItemMrp.setTextColor(ColorStateList.valueOf(res.getColor(R.color.black,null)));
            holder.binding.productListItemSp.setTextColor(ColorStateList.valueOf(res.getColor(R.color.black,null)));
            holder.binding.productListItemSubcategory.setTextColor(ColorStateList.valueOf(res.getColor(R.color.black,null)));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (isOpen){
                        closeExtendedMenu(holder);
                    }
                    else{
                        openExtendedMenu(holder);
                    }
            }
        });


        holder.binding.productListItemExtendedRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(mCtx);
                dialog.setCancelable(false);

                dialog.setContentView(R.layout.extended_remove_dialog);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.getWindow().setWindowAnimations(R.style.AnimationsForDialog);

                Button confirmBtn=dialog.findViewById(R.id.extended_remove_confirmBtn);
                Button cancelBtn=dialog.findViewById(R.id.extended_remove_cancelBtn);
                FloatingActionButton closeBtn=dialog.findViewById(R.id.extended_remove_closeBtn);

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeExtendedMenu(holder);
                        dialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeExtendedMenu(holder);
                        dialog.dismiss();
                    }
                });

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key=productKeyList.get(position);

                        productRef.child(current_user).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                    else{
                                        Log.d("ProductListItem_Remove", "onComplete: "+task.getException().getMessage());
                                        Toast.makeText(mCtx, "Couldn't delete the item", Toast.LENGTH_SHORT).show();
                                    }
                            }
                        });

                    }
                });

                dialog.show();


            }
        });


        holder.binding.productListItemExtendedEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeExtendedMenu(holder);
                Intent intent=new Intent(mCtx, EditProductActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("sold", model.getSoldCount());
                intent.putExtra("total",model.getTotalAvailableQuantity());
                intent.putExtra("soldPercent",soldPercentage);
                mCtx.startActivity(intent);
            }
        });



    }

    private void openExtendedMenu(ProductListItemViewHolder holder) {
        TransitionManager.beginDelayedTransition(holder.binding.productListItemRoot);
        constraintSetShow.applyTo(holder.binding.productListItemRoot);
        isOpen=true;
    }

    private void closeExtendedMenu(ProductListItemViewHolder holder){
        TransitionManager.beginDelayedTransition(holder.binding.productListItemRoot);
        constraintSetHide.applyTo(holder.binding.productListItemRoot);
        isOpen=false;

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void makeUnavailable(int position) {
        ProductListModel model=productList.get(position);
        String key=productKeyList.get(position);
        Map<String,Object> map=new HashMap<>();
        map.put("available",false);


        if (model.isAvailable()){
            productRef.child(current_user).child(key).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        notifyDataSetChanged();
                        Toast.makeText(mCtx, "Item marked unavailable", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(mCtx, "Couldn't make the change", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        else{
            notifyDataSetChanged();
        }

    }

    public void makeAvailable(int position) {
        ProductListModel model=productList.get(position);
        String key=productKeyList.get(position);
        Map<String,Object> map=new HashMap<>();
        map.put("available",true);


        if (!model.isAvailable()){
            productRef.child(current_user).child(key).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        notifyDataSetChanged();
                        Toast.makeText(mCtx, "Item marked Available", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(mCtx, "Couldn't make the change", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else{
            notifyDataSetChanged();
        }

    }

    public static class ProductListItemViewHolder extends RecyclerView.ViewHolder {
        ProductListItemBinding binding;
        public ProductListItemViewHolder(ProductListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}
