package com.flashotech.byerconsole.Activities;

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
import com.flashotech.byerconsole.ByerConsole;
import com.flashotech.byerconsole.Models.NotificationModel;
import com.flashotech.byerconsole.R;
import com.flashotech.byerconsole.databinding.ActivityNotificationBinding;
import com.flashotech.byerconsole.databinding.NotifiactionListItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private FirebaseRecyclerOptions<NotificationModel> option;
    private FirebaseRecyclerAdapter<NotificationModel,NotificationViewHolder> adapter;
    private Query query;
    private Map<String,Object> map=new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        query= ByerConsole.catalogueItemRequestRef.child("All").orderByChild("solved");
        option=new FirebaseRecyclerOptions.Builder<NotificationModel>().setQuery(query,NotificationModel.class).build();
        adapter=new FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull NotificationModel model) {

                if (model.isSolved()){
                    holder.notifiactionListItemBinding.notificationListItemLayout.setBackgroundColor(ContextCompat.getColor(NotificationActivity.this, R.color.white));
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


                        holder.notifiactionListItemBinding.notificationListItemLabel.setText("Request raised for adding catalogue item");
                        holder.notifiactionListItemBinding.notificationListItemDate.setText(model.getDate());
                        holder.notifiactionListItemBinding.notificationListItemName.setText(model.getName());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String key= adapter.getRef(position).getKey();
                                String raisedBy=model.getRaisedBy();

                                map.put("solved",true);
                                ByerConsole.catalogueItemRequestRef.child("All").child(key).updateChildren(map);
                                ByerConsole.catalogueItemRequestRef.child(raisedBy).child(key).updateChildren(map);


                            }
                        });


            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new NotificationViewHolder(NotifiactionListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.notificationList.setAdapter(adapter);
    }


    private void  init() {
        // recycler view setup
            binding.notificationList.setHasFixedSize(true);
            binding.notificationList.setLayoutManager(new LinearLayoutManager(this));

    }

    private class NotificationViewHolder extends RecyclerView.ViewHolder {
        NotifiactionListItemBinding notifiactionListItemBinding;
        public NotificationViewHolder(NotifiactionListItemBinding b) {
            super(b.getRoot());
            notifiactionListItemBinding=b;
        }
    }
}