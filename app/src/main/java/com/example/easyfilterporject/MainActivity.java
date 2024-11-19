package com.example.easyfilterporject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ImageView imageViewGallery;
    private ImageButton buttonBack;
    private TextView textViewName;

    private ActivityResultLauncher<String[]> permissionsLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionsLauncher;
    private static final int GALLERY_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa os elementos da UI
        initUI();

        // Configura os Launchers de permissões e galeria
        setupLaunchers();

        // Configura o comportamento do botão de voltar
        setupButtonBack();

        // Configura a exibição dos dados do usuário
        setupUser();

        // Configura as ações de clique nos ícones
        setupIconListeners();
    }

    private void initUI() {
        auth = FirebaseAuth.getInstance();
        textViewName = findViewById(R.id.textViewName);
        imageViewGallery = findViewById(R.id.imageViewGallery);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupLaunchers() {
        // Launcher para a galeria
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        loadImage(selectedImageUri);
                    }
                }
        );

        // Launcher para permissões
        permissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (boolean granted : permissions.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permissões necessárias não foram concedidas", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para permissões de câmera
        cameraPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permissões não concedidas", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupButtonBack() {
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInScreen.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            usersRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
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

        String name = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");

        if (name != null && !name.isEmpty()) {
            textViewName.setText("Welcome: " + name);
        } else {
            textViewName.setText("Welcome: " + email);
        }
    }

    private void setupIconListeners() {
        // Ícone para tirar foto
        findViewById(R.id.iconTakePhoto).setOnClickListener(v ->
                cameraPermissionsLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
        );

        // Ícone para abrir galeria
        findViewById(R.id.iconOpenGallery).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            galleryLauncher.launch(intent); // Abre a GalleryActivity
        });

        // Ícone para aplicar filtro
        findViewById(R.id.iconApplyFilter).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivity(intent);
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1); // Código arbitrário para chamar a câmera
    }

    private void loadImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageViewGallery.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Se a galeria foi chamada
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedImageUriString = data.getStringExtra("selectedImageUri");

            if (selectedImageUriString != null) {
                Uri selectedImageUri = Uri.parse(selectedImageUriString);
                loadImage(selectedImageUri);
            }
        }

        // Se a câmera foi chamada
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewGallery.setImageBitmap(imageBitmap); // Exibe a foto capturada
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this, SignInScreen.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void openAdminPanel(String email) {
        if ("jessicamaximo23@gmail.com".equals(email)) {
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            startActivity(intent);
        }
    }
}
