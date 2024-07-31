package com.flashotech.byerconsole.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.flashotech.byerconsole.Helper.ImageChooser;
import com.flashotech.byerconsole.R;
import com.flashotech.byerconsole.databinding.ActivityCatalogueBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static com.flashotech.byerconsole.ByerConsole.catalogueRef;
import static com.flashotech.byerconsole.ByerConsole.storageReference;

public class CatalogueActivity extends AppCompatActivity {

    private static final String CATEGORY_TAG= "category";
    private static final String SUB_CATEGORY_TAG= "sub-category";

    private ActivityCatalogueBinding binding;
    private String category;
    private String subCategory;
    private int subCategoryArray;
    private static final int CAMERA_PERMISSION_CODE=101;
    private int request_code;
    private ProgressDialog progressDialog ;
    private String cameraImageFilePath=null;
    private String generatedFilepath;
    private StorageReference filepath;
    private File image;
    private Uri imageUri=null;
    private boolean imageChoosen=false;
    private String name,brand;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCatalogueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();



        binding.itemImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]= new CharSequence[]{"Take a picture","Select from gallery"};
                AlertDialog.Builder builder=new AlertDialog.Builder(CatalogueActivity.this);
                builder.setTitle("Choose an option");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                            {
                                checkPermission(Manifest.permission.CAMERA,
                                        CAMERA_PERMISSION_CODE);
                                break;
                            }
                            case 1:
                            {
                                openGallery();
                                break;
                            }
                        }
                    }
                }).show();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate()){

                    try {
                        uploadCatalogue();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void uploadCatalogue() throws IOException {
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait while the upload completes");
        progressDialog.show();

        switch (request_code){
            case 0: // get image from camera

            {

                if (cameraImageFilePath!=null){
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
                                    Toast.makeText(CatalogueActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                Log.d("request_code",request_code+"");

                break;
            }
            case 1:
            {


                filepath = storageReference.child("Catalogue").child(Calendar.getInstance().getTime() + ".jpeg");


                if (imageUri != null) {

                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();
                    filepath.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                            if (task.isSuccessful()) {
                                getDownloadUrl(filepath);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(CatalogueActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                break;
            }
        }


    }

    private void getDownloadUrl(StorageReference filepath) {

        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                generatedFilepath=uri.toString();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                HashMap<String, Object> itemMap = new HashMap<>();
                itemMap.put("image",generatedFilepath);
                itemMap.put("name",name);
                itemMap.put("subcategory",subCategory);
                itemMap.put("brand",brand);

                catalogueRef.child(category).push().setValue(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();

                            Toast.makeText(CatalogueActivity.this, "Item Added Successfully!", Toast.LENGTH_SHORT).show();
                            binding.itemName.setText(null);
                            binding.itemBrand.setText(null);
                            binding.itemImage.setImageDrawable(ContextCompat.getDrawable(CatalogueActivity.this,R.drawable.ic_add_image));

                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(CatalogueActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            }
        });

    }

    private boolean Validate() {
        boolean isValid=true;
        name=binding.itemName.getText().toString();
        brand=binding.itemBrand.getText().toString();
        if (binding.subCategorySpinner.getSelectedItemPosition()==0){
            Toast.makeText(CatalogueActivity.this, "Please select a sub-category", Toast.LENGTH_SHORT).show();
            isValid= false;
        }
        else{
            subCategory=binding.subCategorySpinner.getSelectedItem().toString();
        }



        if (!imageChoosen){
            isValid=false;
            Toast.makeText(this, "Please Choose an image", Toast.LENGTH_SHORT).show();
        }

        if (name.length()<3){
            binding.itemName.setError("Name can't be less than 3 characters");
            return false;
        }
        if (TextUtils.isEmpty(brand)){
            binding.itemBrand.setError("Enter the name of the brand");
            return false;
        }
        return isValid;
    }

    private void init() {
        // getting data from intent
        category=getIntent().getStringExtra(CATEGORY_TAG);
        subCategoryArray=getIntent().getIntExtra(SUB_CATEGORY_TAG,0);

        //Progress dialog definition
        progressDialog=new ProgressDialog(this);


        // generating spinner elements
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        getResources().getStringArray(subCategoryArray)); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        binding.subCategorySpinner.setAdapter(spinnerArrayAdapter);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.flashotech.byerconsole.provider", photoFile);
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
        image = null;
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
        cameraImageFilePath = image.getAbsolutePath();


        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode==RESULT_OK){

            imageChoosen=true;
            switch (request_code){
                case 0:
                {
                    if (cameraImageFilePath!=null){
                        Bitmap bitmap= BitmapFactory.decodeFile(cameraImageFilePath);
                        binding.itemImage.setImageBitmap(bitmap);
                    }
                    break;
                }
                case 1:
                {
                    assert data != null;
                    imageUri=data.getData();
                    if (imageUri!=null){
                        Picasso.get().load(imageUri).into(binding.itemImage);
                    }
                    break;
                }
            }
            if (resultCode==RESULT_CANCELED){

                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void openGallery() {
        request_code=1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), request_code);
    }
}