package com.example.easyfilterporject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ImageView imageViewGallery;
    private ImageButton buttonBack;
    private TextView textViewName;
    private Bitmap capturedImageBitmap;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionsLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa os elementos da UI
        initUI();

        // Configura os Launchers de permissões e atividades
        setupLaunchers();

        // Configura os ícones e interações
        setupIconListeners();

        // Configura o botão de voltar
        setupButtonBack();

        // Configura o nome do usuário
        setupUser();
    }

    private void initUI() {
        auth = FirebaseAuth.getInstance();
        textViewName = findViewById(R.id.textViewName);
        imageViewGallery = findViewById(R.id.imageViewGallery);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        loadImage(selectedImageUri);
                    }
                }
        );

        cameraPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permissions not granted", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            capturedImageBitmap = photo;
                            imageViewGallery.setImageBitmap(photo); // Exibe a foto tirada
                        }
                    }
                }
        );
    }

    private void setupIconListeners() {
        // Ícone para abrir a câmera
        findViewById(R.id.iconTakePhoto).setOnClickListener(v ->
                cameraPermissionsLauncher.launch(new String[]{Manifest.permission.CAMERA}));

        // Ícone para abrir a galeria
        findViewById(R.id.iconOpenGallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent); // Abre a galeria
        });

        // Ícone para aplicar filtro
        findViewById(R.id.iconApplyFilter).setOnClickListener(v -> {
            if (capturedImageBitmap != null) {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                // Salva a imagem capturada para uso no filtro
                File tempFile = saveBitmapToFile(capturedImageBitmap);
                intent.putExtra("imagePath", tempFile.getAbsolutePath());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Capture or select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonBack() {
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInScreen.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            textViewName.setText("Welcome, " + currentUser.getEmail());
        } else {
            textViewName.setText("Welcome, Guest");
        }
    }

    private void loadImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageViewGallery.setImageBitmap(bitmap);
            capturedImageBitmap = bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        File tempFile = new File(getCacheDir(), "temp_image.png");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent); // Abre a câmera
    }
}
