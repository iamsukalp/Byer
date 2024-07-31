package com.byer.byerretailer.Activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.byer.byerretailer.Adapters.RegisterItemSearchAdapter;
import com.byer.byerretailer.Dialogs.ReportCatalogueItemDialog;
import com.byer.byerretailer.Models.RegisterItemSearchModel;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.ActivityRegisterItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RegisterItemActivity extends AppCompatActivity {

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private static final int CAMERA_PERMISSION_CODE = 101;

    private static final String URI_TAG="imageUri";
    private static final String NAME_TAG="imageName";
    private static final String SEARCHED_TAG="isSearched";
    private static final String PREF_TAG= "MyPref";
    private static final String CATEGORY_TAG= "Category";

    View rootLayout;

    private int revealX;
    private int revealY;
    private int request_code=0;

    private  ActivityRegisterItemBinding binding;
    private Uri imageUri=null;
    private String itemName=null;
    private DatabaseReference catalogRef;
    private Query query;
    private SharedPreferences pref;
    private String category;

    private List<RegisterItemSearchModel> searchList;
    private RegisterItemSearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= ActivityRegisterItemBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        revealAnimation(savedInstanceState);
        init();

        binding.registerItemReportItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        openReportDialog(itemName);
            }
        });

        binding.registerItemBannerReportItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openReportDialog(itemName);
            }
        });

        binding.registerItemGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.registerItemBannerGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.registerItemCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_CODE);
            }
        });
        binding.registerItemBannerCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_CODE);
            }
        });


        binding.registerItemSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemName=binding.registerItemSearchBar.getText().toString().trim();
                if (TextUtils.isEmpty(itemName)){
                    binding.registerItemSearchBar.setError("Can't be left empty");
                }
                else{

                    query=catalogRef.child(category).orderByChild("name").startAt(itemName.toUpperCase()
                    ).endAt(itemName.toLowerCase()+"\uf8ff");
                    binding.registerItemSearchBtn.setVisibility(View.GONE);
                    binding.registerItemProgressBtn.setVisibility(View.VISIBLE);
                    ((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getRootView().getWindowToken(), 0);
                    getResult(query);

                }


            }
        });
    }

    private void openReportDialog(String itemName) {
        ReportCatalogueItemDialog dialog=new ReportCatalogueItemDialog(this,itemName);
        dialog.show();
    }


    private void init() {

        // auto focus editText
        binding.registerItemSearchBar.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        //get data from shared pref
        pref=this.getSharedPreferences(PREF_TAG,0);
        category=pref.getString(CATEGORY_TAG,null);

        // firebase definition
        catalogRef= FirebaseDatabase.getInstance().getReference().child("Catalogue");
        catalogRef.keepSynced(true);

        // recycler view setup
        binding.registerItemResultList.setHasFixedSize(true);
        binding.registerItemResultList.setNestedScrollingEnabled(false);
        binding.registerItemResultList.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<>();
        adapter = new RegisterItemSearchAdapter(this, searchList,category);
        binding.registerItemResultList.setAdapter(adapter);

    }

    private void getResult(Query query) {

        query.addListenerForSingleValueEvent(valueEventListener);

    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            searchList.clear();
            if (dataSnapshot.exists()) {
                binding.registerItemViewSwitcher.setDisplayedChild(0);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RegisterItemSearchModel model = snapshot.getValue(RegisterItemSearchModel.class);
                    searchList.add(model);
                }
                adapter.notifyDataSetChanged();
                binding.registerItemViewSwitcher.setVisibility(View.VISIBLE);
                binding.registerItemSearchBtn.setVisibility(View.VISIBLE);
                binding.registerItemProgressBtn.setVisibility(View.GONE);
            }
            else{
                binding.registerItemSearchBtn.setVisibility(View.VISIBLE);
                binding.registerItemProgressBtn.setVisibility(View.GONE);
                binding.registerItemViewSwitcher.setDisplayedChild(1);
                binding.registerItemViewSwitcher.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(RegisterItemActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };






    private void revealAnimation(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(getColor(R.color.logo_color));
        getWindow().setNavigationBarColor(getColor(R.color.logo_color));

        final Intent intent = getIntent();

        rootLayout=binding.searchLayout;
        if (savedInstanceState == null && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    private void revealActivity(int revealX, int revealY) {
        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, revealX, revealY, 0, finalRadius);
        circularReveal.setDuration(400);
        circularReveal.setInterpolator(new AccelerateInterpolator());

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();

        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                /*activitySearchBinding.searchLayout.setBackgroundColor(getColor(R.color.black));*/
            }
        });

    }

    @Override
    public void onBackPressed() {

        getWindow().setStatusBarColor(getColor(R.color.white));
        getWindow().setNavigationBarColor(getColor(R.color.white));

        int cx = rootLayout.getWidth();
        int cy = 0;
        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, revealX, revealY, finalRadius, 0);

        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rootLayout.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        circularReveal.setDuration(400);
        circularReveal.start();
    }

    private void openGallery() {
        request_code=1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), request_code);
    }

    public void checkPermission(String permission, int requestCode) // checking for camera permission
    {
        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            openCamera();
        }
    }

    private void openCamera() {
        request_code = 0;
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.byer.byerretailer.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        request_code);
            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageUri=Uri.fromFile(image);
        Log.d("camera", "createImageFile: "+imageUri);

        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK) {
            {

                if (requestCode == 1) {
                        imageUri=data.getData();
                }
                if (imageUri != null) {
                    sendData();
                }

            }
        }
    }

    private void sendData() {

        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra(URI_TAG,imageUri.toString());
        intent.putExtra(NAME_TAG,itemName);
        intent.putExtra(SEARCHED_TAG,false);
        startActivity(intent);

    }


}