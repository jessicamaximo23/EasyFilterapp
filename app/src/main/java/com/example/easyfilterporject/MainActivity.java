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

        findViewById(R.id.iconTakePhoto).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OpenCameraActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.iconOpenGallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(intent);
        });

        findViewById(R.id.iconApplyFilter).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivity(intent);
        });
    }



    @Override
    public void onBackPressed() {
       Intent intent = new Intent(MainActivity.this, SignInScreen.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }









    private void openAdminPanel(String email) {
        if (email.equals("jessicamaximo23@gmail.com")) {
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            startActivity(intent);
        }
    }
}