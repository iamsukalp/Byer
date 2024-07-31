package com.byer.byerretailer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Activities.ProductDetailsActivity;
import com.byer.byerretailer.Models.RegisterItemSearchModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.RegisterItemSearchResultItemBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RegisterItemSearchAdapter extends RecyclerView.Adapter<RegisterItemSearchAdapter.RegisterItemSearchResultViewHolder> {

    private Context mCtx;
    private List<RegisterItemSearchModel> searchList;
    private static final String IMAGE_TAG="imageURL";
    private static final String NAME_TAG="imageName";
    private static final String SEARCHED_TAG="isSearched";
    private static final String SUBCATEGORY_TAG="subcategory";
    private String category;
    private static final String CATEGORY_TAG= "category";


    public RegisterItemSearchAdapter(Context mCtx, List<RegisterItemSearchModel> searchList, String category) {
        this.mCtx = mCtx;
        this.searchList = searchList;
        this.category= category;
    }


    @NonNull
    @Override
    public RegisterItemSearchAdapter.RegisterItemSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RegisterItemSearchResultViewHolder(RegisterItemSearchResultItemBinding.inflate(LayoutInflater.from(mCtx),parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull RegisterItemSearchAdapter.RegisterItemSearchResultViewHolder holder, int position) {
        RegisterItemSearchModel model=searchList.get(position);


        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.searchResultBinding.registerItemSearchItemImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder).into(holder.searchResultBinding.registerItemSearchItemImage);
                    }
                });

         holder.searchResultBinding.registerItemSearchItemName.setText(model.getName());
         holder.searchResultBinding.registerItemSearchItemBrand.setText(model.getBrand());

         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent=new Intent(mCtx, ProductDetailsActivity.class);
                 intent.putExtra(NAME_TAG,model.getName());
                 intent.putExtra(SEARCHED_TAG,true);
                 intent.putExtra(IMAGE_TAG,model.getImage());
                 intent.putExtra(SUBCATEGORY_TAG,model.getSubcategory());
                 mCtx.startActivity(intent);

             }
         });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public class RegisterItemSearchResultViewHolder extends RecyclerView.ViewHolder {
        RegisterItemSearchResultItemBinding searchResultBinding;
        public RegisterItemSearchResultViewHolder(RegisterItemSearchResultItemBinding b) {
            super(b.getRoot());
            searchResultBinding=b;
        }
    }
}
