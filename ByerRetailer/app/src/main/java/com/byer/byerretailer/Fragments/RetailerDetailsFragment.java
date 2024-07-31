package com.byer.byerretailer.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.byer.byerretailer.Activities.HomeActivity;
import com.byer.byerretailer.Adapters.CustomSpinnerAdapter;
import com.byer.byerretailer.R;
import com.byer.byerretailer.databinding.FragmentRetailerDetailsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static java.util.Locale.*;


public class RetailerDetailsFragment extends Fragment {

    private FragmentRetailerDetailsBinding binding;
    private final static  int CAMERA_PERMISSION_CODE=101;
    private static final int REQUEST_LOCATION = 1;
    private static final String PREF_TAG= "MyPref";
    private static final String CATEGORY_TAG= "Category";
    private FirebaseAuth mAuth;

    private int request_code = 0;
    private File image;
    private Uri imageUri = null;
    private String phone,token,name,email,category;
    private DatabaseReference retailerRef,geoRef;
    private StorageReference storageReference,filepath;
    private ProgressDialog progressDialog;
    private String generatedFilepath;

    // location related variables

    private GeoFire geoFire;
    private double lat,longi;
    private String sublocality;
    private String address;
    private LocationManager locationManager;
    private String retailerId;

    // stores the image database icons
    private CustomSpinnerAdapter adapter;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // Fused location provider
    private FusedLocationProviderClient fusedLocationProviderClient;



    public RetailerDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentRetailerDetailsBinding.inflate(inflater,container,false);

        init();
        return binding.getRoot();
    }

    private void init() {

        //initializing custom spinner
        //  initializeImageList();

        //ui components
        progressDialog=new ProgressDialog(requireActivity());

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.category_spinner, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.retailerDetailsSpinner.setAdapter(adapter);


        // safe arg variables
        assert getArguments() != null;
        phone=RetailerDetailsFragmentArgs.fromBundle(getArguments()).getPhone();
        token=RetailerDetailsFragmentArgs.fromBundle(getArguments()).getToken();

        // firebase components
        mAuth=FirebaseAuth.getInstance();
        retailerId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        retailerRef= FirebaseDatabase.getInstance().getReference().child("Retailers");
        geoRef=FirebaseDatabase.getInstance().getReference().child("GeoFire").child("Retailers");
        storageReference= FirebaseStorage.getInstance().getReference();

        // Location initiation
        ActivityCompat.requestPermissions( requireActivity(),
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(requireActivity());

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        findLocation();


        // shared pref definition
        pref=requireActivity().getSharedPreferences(PREF_TAG,0);
        editor=pref.edit();



    }

    private void findLocation() {
        // find retailer's location

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            setGeoLocation();
        }
    }

    private void setGeoLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLastLocation();

        }
    }

    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask=fusedLocationProviderClient.getLastLocation();
        locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                if (task.isSuccessful()){
                    Location location=task.getResult();
                            lat = location.getLatitude();
                    longi = location.getLongitude();

                    geoFire=new GeoFire(geoRef);

                    geoFire.setLocation(retailerId, new GeoLocation(lat, longi), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error==null){

                                Geocoder gcd = new Geocoder(requireActivity(), Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = gcd.getFromLocation(lat, longi, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                assert addresses != null;
                                if (addresses.size() > 0) {
                                    sublocality=addresses.get(0).getSubLocality();
                                    address=addresses.get(0).getAddressLine(0);

                                    if (sublocality==null){
                                        sublocality=address;
                                    }
                                }
                                else {
                                    Toast.makeText(requireActivity(), "Error Getting Geolocation", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else{
                                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }


        });
    }


    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }






    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.retailerDetailsShopImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionDialog();
            }
        });
        binding.retailerDetailsEditImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionDialog();
            }
        });
        binding.retailerDetailsSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate())
                    uploadRetailerData();

            }
        });

        binding.retailerDetailsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0){
                    category=parent.getSelectedItem().toString().trim();
                }
                else{
                    Toast.makeText(requireActivity(), "Please select a category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




    }

    private boolean Validate() {
        name=binding.retailerDetailsShopName.getText().toString().trim();
        email=binding.retailerDetailsEmail.getText().toString().trim();


        Log.d("Values", "Validate: "+name+"|"+email+"|"+category+"|"+token);

        // TODO: Write validation subroutine
        return true;
    }

    private void uploadRetailerData() {
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait while the upload completes");
        progressDialog.show();
        if (imageUri!=null){
            filepath = storageReference.child("Retailer Profile").child(Calendar.getInstance().getTime() + ".jpeg");
            try {

                Bitmap bmp = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
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
                            Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    private void getDownloadUrl(StorageReference filepath) {
        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                generatedFilepath = uri.toString();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                //putting category detail in shared pref
                editor.putString(CATEGORY_TAG,category);
                editor.commit();

                HashMap<String, Object> retailerMap = new HashMap<>();
                retailerMap.put("image",generatedFilepath);
                retailerMap.put("name",name);
                retailerMap.put("category",category);
                retailerMap.put("email",email);
                retailerMap.put("token",token);
                retailerMap.put("phone",phone);
                retailerMap.put("sublocality",sublocality);
                retailerMap.put("address",address);



                retailerRef.child(category).child(retailerId).setValue(retailerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent intent=new Intent(requireActivity(), HomeActivity.class);
                            requireActivity().startActivity(intent);
                            requireActivity().finish();

                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            }
        });


    }

    private void openOptionDialog() {
        // option menu for editing image
        final Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_edit_option_menu);
        dialog.setTitle("Pick image from");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.change_shop_availability_dialog_bg);


        ImageView imageCamera = (ImageView) dialog.findViewById(R.id.imageViewCamera);
        ImageView imageGallery = (ImageView) dialog.findViewById(R.id.imageViewGallery);

        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_CODE);
                dialog.dismiss();
            }
        });

        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void openGallery() {
        request_code=1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), request_code);
    }

    // checking for camera permission
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(requireActivity(), permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(requireActivity(),
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
        if (pictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireActivity(), "com.byer.byerretailer.provider", photoFile);
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
                        getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = null;
        try {
            image = File.createTempFile(
                    imageFileName,   //prefix
                    ".jpg",          //suffix
                    storageDir       //directory
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        imageUri=Uri.fromFile(image);

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            switch (request_code){
                case 0:     // get image from camera
                    if (image!=null) {
                        binding.retailerDetailsShopImage.setImageURI(imageUri);
                    }
                    break;
                case 1:     imageUri = Objects.requireNonNull(data).getData();

                    if (imageUri != null) {
                        binding.retailerDetailsShopImage.setImageURI(imageUri);
                    }
                    break;
            }
        }

    }
}