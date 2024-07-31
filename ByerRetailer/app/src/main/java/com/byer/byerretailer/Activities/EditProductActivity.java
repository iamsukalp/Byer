package com.byer.byerretailer.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;

import com.byer.byerretailer.databinding.ActivityEditProductBinding;

public class EditProductActivity extends AppCompatActivity {

    private ActivityEditProductBinding binding;
    private String sold,total,name;
    private int soldPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        binding.editItemResetSwitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float guide= (float) (seekBar.getProgress()/100.0);
                binding.guideline.setGuidelinePercent(guide);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                binding.editItemTotalCountContainer.setAlpha(1.0f);

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {



                if (seekBar.getProgress()>=80){
                    seekBar.setProgress(100,true);
                    seekBar.animate().alpha(0.0f).setDuration(800);
                    binding.shimmerViewText.animate().alpha(0.0f).setDuration(800);
                    seekBar.setEnabled(false);
                    binding.editItemTotalCountCancel.animate().alpha(1.0f).setDuration(800);
                    binding.editItemTotalCountCancel.setEnabled(true);
                }
                else{
                    seekBar.setProgress(0,true);
                    binding.guideline.setGuidelinePercent(0);


                }
            }
        });

        binding.editItemTotalCountCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.editItemTotalCountContainer.animate().alpha(0.0f).setDuration(800);
                binding.editItemTotalCountCancel.setEnabled(false);
                binding.editItemResetSwitch.animate().alpha(1.0f).setDuration(800);
                binding.shimmerViewText.animate().alpha(1.0f).setDuration(800);
                binding.editItemResetSwitch.setEnabled(true);
                binding.editItemResetSwitch.setProgress(0);
                binding.guideline.setGuidelinePercent(0);


            }
        });

        binding.editItemSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: item detail save functionality
            }
        });
    }


    private void init() {
            // get product details from intent
        name=getIntent().getStringExtra("name");
        total=getIntent().getStringExtra("total");
        sold=getIntent().getStringExtra("sold");
        soldPercent= getIntent().getIntExtra("soldPercent",0);

        showItemDetails();

    }

    private void showItemDetails() {
            binding.editItemName.setText(name);
            binding.editItemSoldLegend.setText("Sold : "+sold);
            binding.editItemTotalLegend.setText("Total : "+total);
            binding.editItemSoldPercentText.setText(soldPercent+"%\nSold");

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.editItemSoldPercentage.setProgress(soldPercent,true);
                }
                else{
                    binding.editItemSoldPercentage.setProgress(soldPercent);
                }
            }
        },500);

    }


}