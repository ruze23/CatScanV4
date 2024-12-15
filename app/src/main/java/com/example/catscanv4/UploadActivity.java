package com.example.catscanv4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UploadActivity extends AppCompatActivity implements Detector.DetectorListener, Detector.OnModelSetupListener {

    Button upload, btnUpload, home, logs;
    ImageView uploadPreview;
    Bitmap image,correctedBitmap;
    Detector detector;
    OverlayView overlay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);

        upload = findViewById(R.id.btnUploadImg);
        uploadPreview = findViewById(R.id.uploadedImg);

        btnUpload = findViewById(R.id.btnUpload);
        home = findViewById(R.id.btnHome);
        logs = findViewById(R.id.btnLogs);

        overlay = findViewById(R.id.overlayUp);

        detector = new Detector(getBaseContext(), Constants.MODEL_PATH,Constants.LABELS_PATH,this,this);
        detector.SetupAsync();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                }else{
                    imagePicker();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Upload", Toast.LENGTH_SHORT).show();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadActivity.this, LogsActivity.class);
                startActivity(intent);
            }
        });
    }
    private void imagePicker(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            imagePicker();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(requestCode == 1){
            Uri dat = data.getData();

          Glide.with(this)
                  .load(dat)
                  .into(uploadPreview);

//          uploadPreview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//              @Override
//              public void onGlobalLayout() {
//                  uploadPreview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                  int imageWidth = uploadPreview.getWidth();
//                  int imageHeight = uploadPreview.getHeight();
//
//
//              }
//          });

            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),dat);
                detector.detect(image);
//                correctedBitmap = fixRotation(dat, image);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private Bitmap fixRotation(Uri imageUri, Bitmap image) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(imageUri);

        if(inputStream != null){
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Log.d("ImageOrientation", "Orientation: " + orientation);

            switch(orientation){
                case ExifInterface.ORIENTATION_ROTATE_90: return rotateBitmap(image, 90);

                case ExifInterface.ORIENTATION_ROTATE_180: return rotateBitmap(image, 180);

                case ExifInterface.ORIENTATION_ROTATE_270: return rotateBitmap(image, 270);

                default: return image;
            }
        }
       return image;
    }


    private Bitmap rotateBitmap(Bitmap bitmap, int degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    public void onDetect(List<BoundingBox> boundingBoxes){
        Log.i("Detected", "Object Detected");
        overlay.setResults(boundingBoxes);
    }
    public void onEmptyDetect(){
        Log.i("Detected", "No object detected");
        overlay.Clear();
    }

    @Override
    public void OnModelSetupFailed(Exception exception) {
    }

    @Override
    public void OnModelSetupSuccess() {

    }
}