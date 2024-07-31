package com.byer.byerretailer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byerretailer.ByerRetailer;
import com.byer.byerretailer.Models.NotificationsModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.ActivityNotificationBinding;
import com.byer.byerretailer.databinding.NotifiactionListItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;
    private FirebaseAuth mAuth;
    private String retailerId;
    private FirebaseRecyclerOptions<NotificationsModel> options;
    private FirebaseRecyclerAdapter<NotificationsModel,NotificationsViewHolder> adapter;
    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        //firebase components
        mAuth=FirebaseAuth.getInstance();
        retailerId=mAuth.getCurrentUser().getUid();

        query= ByerRetailer.catalogueItemRequestRef.child(retailerId).orderByChild("seen");

        // setting up recyclerview
        binding.notificationsList.setHasFixedSize(true);
        binding.notificationsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        options=new FirebaseRecyclerOptions.Builder<NotificationsModel>().setQuery(query,NotificationsModel.class).build();
        adapter=new FirebaseRecyclerAdapter<NotificationsModel, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position, @NonNull NotificationsModel model) {

                if (model.isSeen()){
                    holder.notifiactionListItemBinding.notificationListItemLayout.setBackgroundColor(ContextCompat.getColor(NotificationActivity.this,R.color.white));
                    holder.notifiactionListItemBinding.notificationListItemName.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.grey));
                    holder.notifiactionListItemBinding.notificationListItemLabel.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.grey));
                    holder.notifiactionListItemBinding.notificationListItemDate.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.grey));
                }
                else{
                    holder.notifiactionListItemBinding.notificationListItemLayout.setBackgroundColor(ContextCompat.getColor(NotificationActivity.this,R.color.logo_color));
                    holder.notifiactionListItemBinding.notificationListItemName.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.white));
                    holder.notifiactionListItemBinding.notificationListItemLabel.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.white));
                    holder.notifiactionListItemBinding.notificationListItemDate.setTextColor(ContextCompat.getColor(NotificationActivity.this,R.color.white));

                }
                if (model.isSolved()){
                    holder.notifiactionListItemBinding.notificationListItemImage.setImageDrawable(ContextCompat.getDrawable(NotificationActivity.this,R.drawable.ic_confirm));
                }
                else{
                    holder.notifiactionListItemBinding.notificationListItemImage.setImageDrawable(ContextCompat.getDrawable(NotificationActivity.this,R.drawable.ic_report_color));
                }
                holder.notifiactionListItemBinding.notificationListItemDate.setText("Date: "+model.getDate());
                holder.notifiactionListItemBinding.notificationListItemLabel.setText("Request raised for adding item to catalogue");
                holder.notifiactionListItemBinding.notificationListItemName.setText("Item Name: "+model.getName());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key= adapter.getRef(position).getKey();
                        Map<String,Object> map=new HashMap<>();
                        map.put("seen",true);
                        ByerRetailer.catalogueItemRequestRef.child(retailerId).child(key).updateChildren(map);
                    }
                });

            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new NotificationsViewHolder(NotifiactionListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        };
        adapter.startListening();
        binding.notificationsList.setAdapter(adapter);

    }

    private static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        NotifiactionListItemBinding notifiactionListItemBinding;
        public NotificationsViewHolder(NotifiactionListItemBinding nb) {
            super(nb.getRoot());
            notifiactionListItemBinding=nb;
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();


    }
}