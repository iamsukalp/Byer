package com.byer.byer.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byer.Activities.ProductListActivity;
import com.byer.byer.Models.RetailerModel;
import com.byer.byer.R;
import com.byer.byer.databinding.RetailerListItemBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RetailerListAdapter extends RecyclerView.Adapter<RetailerListAdapter.RetailerViewHolder> implements Filterable {
    private Activity activity;
    private List<RetailerModel> retailerList;
    private List<RetailerModel> retailerListAll;
    private List<String> keyList;
    private List<String> distanceList;
    private static final String NAME_TAG="name";
    private static final String PHONE_TAG="phone";
    private static final String IMAGE_TAG="image";
    private static final String SUBLOCALITY_TAG="sublocality";
    private static final String DISTANCE_TAG="distance";
    private static final String KEY_TAG="key";
    private static final String CATEGORY_TAG ="category" ;
    private String category;


    public RetailerListAdapter(Activity activity, List<String> keyList, List<RetailerModel> retailerList, List<RetailerModel> retailerListAll, List<String> distanceList, String category) {
        this.activity = activity;
        this.retailerList = retailerList;
        this.retailerListAll=retailerListAll;
        this.distanceList=distanceList;
        this.keyList= keyList;
        this.category=category;
    }

    @NonNull
    @Override
    public RetailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RetailerViewHolder(RetailerListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RetailerViewHolder holder, int position) {
        RetailerModel model=retailerList.get(position);

        holder.retailerListItemBinding.retailerListItemAddressQualifier.setText(model.getSublocality());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.retailerListItemBinding.retailerListItemLogo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder)
                                .into(holder.retailerListItemBinding.retailerListItemLogo);
                    }
                });
        holder.retailerListItemBinding.retailerListItemName.setText(model.getName());
        holder.retailerListItemBinding.retailerListItemDistanceQualifier.setText(distanceList.get(position)+" KMs");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                Intent intent=new Intent(activity, ProductListActivity.class);
                intent.putExtra(NAME_TAG,model.getName());
                intent.putExtra(PHONE_TAG,model.getPhone());
                intent.putExtra(IMAGE_TAG,model.getImage());
                intent.putExtra(SUBLOCALITY_TAG,model.getSublocality());
                intent.putExtra(DISTANCE_TAG,distanceList.get(position));
                intent.putExtra(KEY_TAG,keyList.get(position));
                intent.putExtra(CATEGORY_TAG,model.getCategory());


                Pair[] pairs = new Pair[2];
                pairs[0]=new Pair<View,String>(holder.retailerListItemBinding.retailerListItemLogo,"imageTransition");
                pairs[1]=new Pair<View,String>(holder.retailerListItemBinding.retailerListItemName,"nameTransition");


                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(activity,pairs);

                activity.startActivity(intent,options.toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {


        return retailerList == null ? 0 : retailerList.size();
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter=new Filter() {

        // runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<RetailerModel> filteredList =new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                    filteredList.addAll(retailerListAll);

            }
            else{


                Log.d("Filter", "performFiltering: "+retailerList.size());

                for(RetailerModel model:retailerList){
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
                retailerList.clear();
                retailerList.addAll((List) results.values);
                notifyDataSetChanged();
        }
    };

    public static class RetailerViewHolder extends RecyclerView.ViewHolder {
        RetailerListItemBinding retailerListItemBinding;
        public RetailerViewHolder(RetailerListItemBinding binding) {
            super(binding.getRoot());
           retailerListItemBinding=binding;
        }

    }
}
