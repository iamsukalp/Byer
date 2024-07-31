package com.byer.byerretailer.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.ProductDetailsActivity;
import com.byer.byerretailer.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class UnitsRVAdapter extends RecyclerView.Adapter<UnitsRVAdapter.UnitsViewHolder> {

    ArrayList<String> units;
    Context mContext;

    public UnitsRVAdapter(Context mContext, ArrayList<String> units) {
        this.units = units;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UnitsRVAdapter.UnitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_unit_item, parent, false);
        return new UnitsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitsViewHolder holder, int position) {
        holder.chip.setText(units.get(position));

        holder.chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("item", units.get(position));
                ProductDetailsActivity.binding.productDetailsUnitET.setText(units.get(position));
                ProductDetailsActivity.productDetailsBottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    public static class UnitsViewHolder extends RecyclerView.ViewHolder {

        Chip chip;

        public UnitsViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.unitItemLayoutTV);
        }

    }
}