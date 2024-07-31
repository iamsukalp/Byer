package com.flashotech.byerconsole.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.flashotech.byerconsole.Activities.CatalogueActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageChooser {


    public static Uri imageUri;
    private static Activity ctx;
    private static File image;



    public static void getChooser(Activity activity, int request_code) {
                  ctx=activity;
                if (request_code==0){

                }
                else{

                }
    }

    private static File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
        ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        return image;
    }

}
