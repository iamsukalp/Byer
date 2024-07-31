package com.byer.byerretailer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


import com.byer.byerretailer.Adapters.UnitsRVAdapter;
import com.byer.byerretailer.Models.Units;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.ActivityProductDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String IMAGE_TAG="imageURL";
    private static final String NAME_TAG="imageName";
    private static final String SEARCHED_TAG="isSearched";
    private static final String SUBCATEGORY_TAG="subcategory";
    private static final String URI_TAG="imageUri";
    @SuppressLint("StaticFieldLeak")
    public static ActivityProductDetailsBinding binding;



    private Uri imageUri=null;
    private String imageURL=null;
    private String itemName;
    private boolean isSearched=false;
    private String subcategory=null;
    private String MRP,SP,quantity,unit,desc,totalAvailableQuantity;
    private String generatedImageUrl;

    private String current_user;

    private FirebaseAuth mAuth;
    private DatabaseReference productRef;
    private StorageReference storageReference,filepath;

    private ProgressDialog progressDialog;

    @SuppressLint("StaticFieldLeak")
    public static BottomSheetDialog productDetailsBottomSheetDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        Listeners();

    }

    private void Listeners() {
            binding.productDetailsUnitET.setOnClickListener(this);
            binding.productDetailsAddProductBtn.setOnClickListener(this);
            binding.productDetailsCategorySpinner.setOnItemSelectedListener(this);
    }

    private void init() {
        getWindow().setNavigationBarColor(getColor(R.color.white));
        getWindow().setStatusBarColor(getColor(R.color.logo_color));

        //getting data from intent
        isSearched=getIntent().getBooleanExtra(SEARCHED_TAG,false);
        if (isSearched){
            imageURL=getIntent().getStringExtra(IMAGE_TAG);
        }
        else{
            imageUri= Uri.parse(getIntent().getStringExtra(URI_TAG));
        }
        subcategory=getIntent().getStringExtra(SUBCATEGORY_TAG);
        itemName=getIntent().getStringExtra(NAME_TAG);

        // firebase components
        mAuth=FirebaseAuth.getInstance();
        current_user=mAuth.getCurrentUser().getUid();
        productRef= FirebaseDatabase.getInstance().getReference().child("Products");
        storageReference= FirebaseStorage.getInstance().getReference();

        // progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Adding product to database");
        progressDialog.setCanceledOnTouchOutside(false);

       showDetails();


    }

    private void showDetails() {

        if (isSearched){
            Picasso.get().load(imageURL).placeholder(R.drawable.ic_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(binding.productDetailsProductImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                            Picasso.get().load(imageURL).placeholder(R.drawable.ic_image_placeholder).into(binding.productDetailsProductImage);
                }
            });

        }
        else{
            Picasso.get().load(imageUri).placeholder(R.drawable.ic_image_placeholder).into(binding.productDetailsProductImage);

        }

        binding.productDetailsProductNameET.setText(itemName);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.productDetailsUnitET: {
                showBottomSheet();
                break;
            }
            case R.id.productDetailsAddProductBtn:{
                progressDialog.show();

                if (Validate()){
                    try {
                        if (isSearched){
                            Map<String,Object> productMap=new HashMap<>();
                            productMap.put("image",imageURL);
                            productMap.put("name",itemName);
                            productMap.put("mrp",MRP);
                            productMap.put("sp",SP);
                            productMap.put("quantity",quantity);
                            productMap.put("totalAvailableQuantity",totalAvailableQuantity);
                            productMap.put("subcategory",subcategory);
                            productMap.put("unit",unit);
                            productMap.put("description",desc);
                            productMap.put("available",true);
                            insertItem(productMap);
                        }
                        else{
                            uploadProductImage();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    progressDialog.dismiss();
                }
                break;
            }
        }
    }

    private void insertItem(Map<String, Object> productMap) {


        productRef.child(current_user).push().setValue(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent intent=new Intent(ProductDetailsActivity.this,HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(ProductDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                }
            });
    }

    private void uploadProductImage() throws IOException {

        filepath = storageReference.child("Catalogue").child(Calendar.getInstance().getTime() + ".jpeg");

        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                       getDownloadUrl(filepath);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ProductDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void getDownloadUrl(StorageReference filepath) {

        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                generatedImageUrl=uri.toString();

                Map<String,Object> productMap=new HashMap<>();
                productMap.put("image",generatedImageUrl);
                productMap.put("name",itemName);
                productMap.put("mrp",MRP);
                productMap.put("sp",SP);
                productMap.put("subcategory",subcategory);
                productMap.put("totalAvailableQuantity",totalAvailableQuantity);
                productMap.put("quantity",quantity);
                productMap.put("unit",unit);
                productMap.put("description",desc);
                productMap.put("available",true);
                insertItem(productMap);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private boolean Validate() {

        itemName=binding.productDetailsProductNameET.getText().toString().trim();
        MRP=binding.productDetailsMRPET.getText().toString().trim();
        SP=binding.productDetailsSPET.getText().toString().trim();
        quantity=binding.productDetailsQuantityET.getText().toString().trim();
        unit=binding.productDetailsUnitET.getText().toString().trim();
        desc=binding.productDetailsDescriptionET.getText().toString().trim();
        totalAvailableQuantity=binding.productDetailsTotalAvailableQuantityET.getText().toString().trim();

        // TODO : write validation subroutine
        return true;
    }

    private void showBottomSheet() {
        productDetailsBottomSheetDialog = new BottomSheetDialog(this);
        productDetailsBottomSheetDialog.setContentView(R.layout.bottom_sheet_unit);

        productDetailsBottomSheetDialog.show();

        RecyclerView recyclerView = productDetailsBottomSheetDialog.findViewById(R.id.bottomSheetUnitRV);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(5, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        UnitsRVAdapter adapter = new UnitsRVAdapter(this, Units.getUnits());
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        subcategory=parent.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}