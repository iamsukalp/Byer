package com.byer.byerretailer.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.InProgressOrderActivity;
import com.byer.byerretailer.Models.ItemModel;
import com.byer.byerretailer.databinding.OrderItemsListItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    List<ItemModel> itemList;
    Activity activity;

    public ItemAdapter(InProgressOrderActivity inProgressOrderActivity, List<ItemModel> itemList) {
        this.activity=inProgressOrderActivity;
        this.itemList=itemList;
    }

    @NonNull
    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(OrderItemsListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemViewHolder holder, int position) {
            ItemModel model=itemList.get(position);
        holder.binding.orderItemName.setText(model.getName());
        holder.binding.orderItemCount.setText(model.getCount()+" "+model.getUnit());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        OrderItemsListItemBinding binding;
        public ItemViewHolder(  OrderItemsListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}
