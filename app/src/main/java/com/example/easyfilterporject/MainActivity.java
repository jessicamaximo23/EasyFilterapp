package com.example.easyfilterporject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ImageView imageViewGallery;
    private ImageButton buttonBack;
    private TextView textViewName;
    private Bitmap capturedImageBitmap;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionsLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private GPUImage gpuImage;
    private Matrix matrix = new Matrix();

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

        findViewById(R.id.iconRotate).setOnClickListener(v -> rotateImage(90));
    }

    private void rotateImage(int degrees) {
        if (capturedImageBitmap != null) {
            // Aplica a rotação na imagem
            matrix.postRotate(degrees); // Gira a imagem de acordo com a quantidade de graus

            // Cria uma nova imagem com a rotação aplicada
            Bitmap rotatedBitmap = Bitmap.createBitmap(capturedImageBitmap, 0, 0, capturedImageBitmap.getWidth(), capturedImageBitmap.getHeight(), matrix, true);

            // Atualiza a ImageView com a imagem rotacionada
            imageViewGallery.setImageBitmap(rotatedBitmap);

            // Atualiza a imagem capturada com a imagem rotacionada
            capturedImageBitmap = rotatedBitmap;
        }
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

        // Ícone para salvar foto
        findViewById(R.id.iconSavePhoto).setOnClickListener(v -> {
            if (capturedImageBitmap != null) {
                savePhotoToGallery(capturedImageBitmap);
            } else {
                Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
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

    //save the photo on my cel
    private void savePhotoToGallery(Bitmap resultBitmap) {
        if (resultBitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "filtered_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EasyFilterPhotos");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(this, "Saved to Photos!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            }
        } else {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "EasyFilterPhotos");
            if (!appDir.exists()) appDir.mkdirs();

            File imageFile = new File(appDir, "filtered_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(imageFile));
                sendBroadcast(mediaScanIntent);

                Toast.makeText(this, "Saved to Gallery!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupUser() {
        // Retrieve name user (welcome user)
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            databaseReference.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.getValue(String.class);
                        textViewName.setText("Welcome, " + name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Exibe mensagem padrão em caso de erro
                    textViewName.setText("Welcome, User");
                    Log.e("Firebase Error", "Error " + databaseError.getMessage());
                }
            });
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
