package com.example.easyfilterporject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

    private ActivityResultLauncher<String[]> permissionsLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionsLauncher;
    private ActivityResultLauncher<Intent> filterLauncher;


    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int FILTER_REQUEST_CODE = 1;  // Definindo o código da requisição

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    private Bitmap bitmapToSave; // Variável para armazenar o bitmap


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o ImageView para exibir a imagem
        imageViewGallery = findViewById(R.id.imageViewGallery);

        // Recupera a URI da imagem passada pela GalleryActivity
        String imageUriString = getIntent().getStringExtra("selectedImageUri");

        if (imageUriString != null) {
            Uri selectedImageUri = Uri.parse(imageUriString);

            // Exibe a imagem na HomeActivity
            imageViewGallery.setImageURI(selectedImageUri);
        }

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

        ImageView saveImageButton = findViewById(R.id.iconSavePhoto);
        saveImageButton.setOnClickListener(v -> {
            Bitmap bitmapToSave = ((BitmapDrawable) imageViewGallery.getDrawable()).getBitmap();
            if (bitmapToSave != null) {
                saveImageToStorage(bitmapToSave);
            }
        });

    }

    private boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
    }

    private void initUI() {
        auth = FirebaseAuth.getInstance();
        textViewName = findViewById(R.id.textViewName);
        imageViewGallery = findViewById(R.id.imageViewGallery);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupLaunchers() {

        ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
         new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null ) {
                        Bitmap filteredBitmap = result.getData().getParcelableExtra("filteredBitmap");
                        if (filteredBitmap != null) {
                            imageViewGallery.setImageBitmap(filteredBitmap); // Atualiza a imagem filtrada
                        }
                    }
                }
);
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
                        Toast.makeText(this, "Required permissions were not granted", Toast.LENGTH_SHORT).show();
                    }
                }
        );

//         Launcher para permissões de câmera
        cameraPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
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
            // Verificar se há uma imagem selecionada
            Drawable drawable = imageViewGallery.getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable ) {
                Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

                // Criar intent e passar bitmap
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);

                // Salvar o Bitmap em um arquivo temporário
                File tempFile = new File(getCacheDir(), "temp_image.png");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                intent.putExtra("imagePath", tempFile.getAbsolutePath());
                filterLauncher.launch(intent);

            } else {
                Toast.makeText(this, "Select Image First", Toast.LENGTH_SHORT).show();
            }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    //Save photo after aplly filter
    private void saveImageToStorage(Bitmap bitmap) {
        if (!hasWritePermission()) {
            requestWritePermission();
            return;
        }

        // Caminho do diretório onde as imagens serão salvas
        File imagesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EasyFilter");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs(); // Cria o diretório se não existir
        }

        // Nome do arquivo de imagem
        String fileName = "filtered_image_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(imagesDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            // Comprime o bitmap e escreve no arquivo
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

            // Notifica o usuário que a imagem foi salva
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
        //notify gallery when the photo is finish
        // Notifica a Galeria
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToStorage(bitmapToSave);
            } else {
                Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
