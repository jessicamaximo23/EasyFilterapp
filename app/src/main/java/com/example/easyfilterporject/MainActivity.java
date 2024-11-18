package com.example.easyfilterporject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private  FirebaseAuth auth;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private ImageView imageView;
    private ImageButton buttonBack;
    private TextView textViewName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        textViewName = findViewById(R.id.textViewName);
        imageView = findViewById(R.id.imageView);
        buttonBack = findViewById(R.id.buttonBack);

        String name = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");

        if (name != null && !name.isEmpty()) {
            textViewName.setText("Welcome: " + name);
        } else {
            textViewName.setText("Welcome: " + email);
        }

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, SignInScreen.class);
                startActivity(intent);
                finish();
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            usersRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        // Exibe o nome do usuário
                        String nameFromDb = task.getResult().child("name").getValue(String.class);
                        String emailFromDb = task.getResult().child("email").getValue(String.class);

                        if (nameFromDb != null && !nameFromDb.isEmpty()) {
                            textViewName.setText("Welcome, " + nameFromDb);
                        } else {
                            textViewName.setText("Welcome: " + emailFromDb);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.iconTakePhoto).setOnClickListener(v -> checkCameraPermission());

        ImageView iconOpenGallery = findViewById(R.id.iconOpenGallery);
        //intent for open gallery
        iconOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        ImageView iconApplyFilter = findViewById(R.id.iconApplyFilter);
        // intent for Apply Filter
        iconApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                startActivity(intent);
            }
        });


        ImageView iconTakePhoto = findViewById(R.id.iconTakePhoto);
        //  intent for Take photo
        iconTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }


    @Override
    public void onBackPressed() {
       Intent intent = new Intent(MainActivity.this, SignInScreen.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }


    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.easyfilterporject.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                    Toast.makeText(this, "Permission to use the camera is required to take photos.", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, "Camera permission permanently denied. Go to the app's settings to activate it", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK ) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Captura de imagem
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    imageView.setImageBitmap(imageBitmap);
                } else {
                    Toast.makeText(this, "Error,Try again.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_IMAGE_REQUEST && data != null) {

                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageView.setImageBitmap(selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error Gallery Image.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
    }
    private void openAdminPanel(String email) {
        if (email.equals("jessicamaximo23@gmail.com")) {
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            startActivity(intent);
        }
    }
}