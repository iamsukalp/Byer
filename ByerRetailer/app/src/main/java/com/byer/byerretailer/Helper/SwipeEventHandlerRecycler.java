package com.byer.byerretailer.Helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.byer.byerretailer.Adapters.ProductListItemAdapter;
import com.byer.byerretailer.R;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SwipeEventHandlerRecycler extends ItemTouchHelper.SimpleCallback{
    private ProductListItemAdapter mAdapter;
    private Drawable icon_unavailable,icon_available;
    private ColorDrawable bg_unavailable,bg_available;
    private Context context;

    public SwipeEventHandlerRecycler(ProductListItemAdapter adapter, Context context) {
        super(0,ItemTouchHelper.LEFT |ItemTouchHelper.RIGHT);
        mAdapter=adapter;
        this.context=context;
        icon_unavailable = ContextCompat.getDrawable(context,
                R.drawable.ic_sold_out);
        icon_available=ContextCompat.getDrawable(context,
                R.drawable.ic_in_stock);
        bg_unavailable = new ColorDrawable(Color.RED);
        bg_available=new ColorDrawable(context.getColor(R.color.logo_color));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();

        switch (direction){
            case ItemTouchHelper.LEFT:{
                mAdapter.makeUnavailable(position);
                break;
            }
            case ItemTouchHelper.RIGHT:{
                mAdapter.makeAvailable(position);
                break;
            }
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightBackgroundColor(ContextCompat.getColor(context,R.color.logo_color))
                .addSwipeRightActionIcon(R.drawable.ic_in_stock)
                .addSwipeRightLabel("In `stock")
                .addSwipeLeftLabel("Out of stock")
                .setSwipeLeftLabelColor(ContextCompat.getColor(context,R.color.white))
                .setSwipeRightLabelColor(ContextCompat.getColor(context,R.color.white))
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                .addSwipeLeftActionIcon(R.drawable.ic_sold_out)
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX,
                dY, actionState, isCurrentlyActive);

    }
}
