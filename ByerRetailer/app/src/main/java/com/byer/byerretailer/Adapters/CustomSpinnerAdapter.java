package com.byer.byerretailer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.byer.byerretailer.Models.CategorySpinnerModel;
import com.byer.byerretailer.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<CategorySpinnerModel>
{

    LayoutInflater layoutInflater;


    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<CategorySpinnerModel> category)
    {
        super(context, resource, category);
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View rowView = layoutInflater.inflate(R.layout.spinner_view, null,true);
        CategorySpinnerModel category = getItem(position);
        TextView textView = (TextView)rowView.findViewById(R.id.categorySpinnerTextView);
        ImageView imageView = (ImageView)rowView.findViewById(R.id.categorySpinnerImage);
        textView.setText(category.getName());
        imageView.setImageResource(category.getImage());
        return rowView;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if(convertView == null)
            convertView = layoutInflater.inflate(R.layout.spinner_view, parent,false);

        CategorySpinnerModel category = getItem(position);
        TextView textView = (TextView)convertView.findViewById(R.id.categorySpinnerTextView);
        ImageView imageView = (ImageView)convertView.findViewById(R.id.categorySpinnerImage);
        textView.setText(category.getName());
        imageView.setImageResource(category.getImage());
        return convertView;
    }

}
