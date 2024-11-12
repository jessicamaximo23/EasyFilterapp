package com.example.easyfilterporject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


    private  FirebaseAuth auth;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private ImageButton buttonBack;
    private TextView textViewEmail, textViewName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        imageView = findViewById(R.id.imageView);
        buttonBack = findViewById(R.id.buttonBack);

        // Recupera o nome e o email da Intent
        String name = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");

        // Define o texto nas TextViews
        textViewName.setText("Welcome: " + name);
        textViewEmail.setText("Email: " + email);

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
                        // Recupera o nome do usuário
//                        String name = task.getResult().child("name").getValue(String.class);
//                        String email = task.getResult().child("email").getValue(String.class);

                        // Exibe o nome na TextView
                        if (name != null && !name.isEmpty()) {
                            textViewName.setText("Welcome: " + name);
                        } else {
                            textViewName.setText("Welcome: " + email); // Caso o nome não esteja disponível
                        }

                        // Exibe o email na TextView
                        textViewEmail.setText("Email: " + email);
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
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
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

    @Override
    public void onBackPressed() {
       Intent intent = new Intent(MainActivity.this, SignInScreen.class);
        startActivity(intent);
        finish();
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
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(imageBitmap);
        }
}


    private void openAdminPanel(String email) {
        if (email.equals("jessicamaximo23@gmail.com")) {
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            startActivity(intent);
        }
    }

}