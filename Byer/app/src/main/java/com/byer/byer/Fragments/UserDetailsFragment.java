package com.byer.byer.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.byer.byer.Activities.HomeActivity;
import com.byer.byer.R;
import com.byer.byer.databinding.FragmentUserDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.byer.byer.Byer.geoRef;
import static com.byer.byer.Byer.storageReference;
import static com.byer.byer.Byer.userRef;
import static java.util.Locale.getDefault;


public class UserDetailsFragment extends Fragment {

    private static final int REQUEST_LOCATION = 150;
    private static final int CAMERA_PERMISSION_CODE =101 ;
    private FragmentUserDetailsBinding binding;

    // location related objects
    private LocationManager locationManager;
    private GeoFire geoFire;
    private double lat,longi;
    private String sublocality;
    private String address;

    // Firebase auth
    private FirebaseAuth mAuth;
    private String userId;
    
    private int request_code=0;
    private File image;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private StorageReference filepath;
    private String generatedFilepath;
    private String token;
    private String phone;
    private String name;
    private String email;

    public UserDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentUserDetailsBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();



    }

    private void init() {


        // safe arg variables
        assert getArguments() != null;
        phone=UserDetailsFragmentArgs.fromBundle(getArguments()).getPhone();
        token=UserDetailsFragmentArgs.fromBundle(getArguments()).getToken();


        //getting user id
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        
        // progress dialog
        progressDialog=new ProgressDialog(requireActivity());




    }

    @Override
    public void onStart() {
        super.onStart();
        // Location initiation
        ActivityCompat.requestPermissions( requireActivity(),
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        findLocation();
    }

    private void findLocation() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            setGeoLocation();
        } 
    }

    @Override
    public void onResume() {
        super.onResume();
        findLocation();
    }

    private void setGeoLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                lat = locationGPS.getLatitude();
                longi = locationGPS.getLongitude();

                geoFire=new GeoFire(geoRef.child("Users"));

                geoFire.setLocation(userId, new GeoLocation(lat, longi), new GeoFire.CompletionListener() {
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

                                Map<String,Object> map=new HashMap<>();
                                map.put("sublocality",sublocality);
                                map.put("address",address);

                                userRef.child(userId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d("Location Insert", "onComplete: Done ");
                                        }
                                        else{
                                            Log.d("Location Insert", "onComplete: Not Done ");
                                        }
                                    }
                                });

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

            } else {
                Toast.makeText(requireActivity(), "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_LOCATION){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                setGeoLocation();
            }
        }
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

        binding.userDetailsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionDialog();
            }
        });
        binding.userDetailsEditImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionDialog();
            }
        });
        binding.userDetailsSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate())
                    uploadUserData();

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

    private void checkPermission(String camera, int cameraPermissionCode) {
        if (ContextCompat.checkSelfPermission(requireActivity(), camera)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[] { camera },
                    cameraPermissionCode);
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
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireActivity(), "com.flashotech.byerretailer.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        request_code);
            }
        }
    }

    private File createImageFile() {
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

    private boolean Validate() {
        name=binding.userDetailsName.getText().toString().trim();
        email=binding.userDetailsEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.userDetailsEmail.setError("Please enter a valid email");
            return false;
        }
        if (!TextUtils.isEmpty(name) && name.length()<3){
            binding.userDetailsName.setError("Please enter a valid name");
            return false;
        }

    return true;
    }

    private void uploadUserData() {
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait while the upload completes");
        progressDialog.show();
        if (imageUri!=null){
            filepath = storageReference.child("User Profile").child(Calendar.getInstance().getTime() + ".jpeg");
            try {

                Bitmap bmp = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] data = baos.toByteArray();

                /*  InputStream stream = new FileInputStream(new File(cameraImageFilePath));*/
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

                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("image",generatedFilepath);
                userMap.put("name",name);
                userMap.put("email",email);
                userMap.put("token",token);
                userMap.put("phone",phone);
                userMap.put("sublocality",sublocality);
                userMap.put("address",address);



                userRef.child(userId).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            switch (request_code){
                case 0:     // get image from camera
                    if (image!=null) {
                        binding.userDetailsImage.setImageURI(imageUri);
                    }
                    break;
                case 1:     imageUri = Objects.requireNonNull(data).getData();

                    if (imageUri != null) {
                        binding.userDetailsImage.setImageURI(imageUri);
                    }
                    break;
            }
        }

    }
}