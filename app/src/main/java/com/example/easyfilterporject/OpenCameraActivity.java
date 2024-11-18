package com.example.easyfilterporject;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OpenCameraActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageButton captureButton;
    private ImageButton switchCameraButton;
    private ImageButton backButton;
    private ImageView thumbnailView;
    private Uri lastImageUri;
    private boolean isFrontCamera = false;  // Alternar entre câmera frontal e traseira
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        // Inicializa as views
        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);
        switchCameraButton = findViewById(R.id.switch_camera);
        backButton = findViewById(R.id.back_button);
        thumbnailView = findViewById(R.id.thumbnail_view);

        // Verifica permissão da câmera e inicializa a câmera
        if (checkCameraPermission()) {
            startCamera();
        }

        // Configura os listeners dos botões
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Ao pressionar o botão de captura, tira a foto
        captureButton.setOnClickListener(v -> takePhoto());

        // Alterna entre a câmera frontal e traseira
        switchCameraButton.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;  // Alterna a câmera
            startCamera();  // Reinicia a câmera com a nova configuração
        });

        // Volta para a tela anterior
        backButton.setOnClickListener(v -> {
            if (lastImageUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.setData(lastImageUri);
                setResult(RESULT_OK, resultIntent);
            }
            finish();  // Fecha a activity
        });

        // Mostra a imagem em miniatura quando pressionada
        thumbnailView.setOnClickListener(v -> {
            if (lastImageUri != null) {
                Intent intent = new Intent(this, FilterActivity.class);
                intent.putExtra("imageUri", lastImageUri.toString());
                startActivity(intent);
            }
        });
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Se a permissão não for concedida, solicita a permissão
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Obtém o provedor da câmera
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Configura o preview da câmera
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Configura a captura da imagem
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                // Configura a câmera frontal ou traseira
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                        .build();

                // Vincula os casos de uso à câmera
                cameraProvider.unbindAll();  // Desvincula as câmeras anteriores
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);  // Vincula a nova configuração da câmera

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));  // Executa no executor principal
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // Cria um arquivo para armazenar a foto
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configura o arquivo de saída da imagem
        ContentValues contentValues = new ContentValues();
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Cria um objeto de opções de arquivo de saída
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        .build();

        // Configura o ouvinte de captura da imagem
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e("OpenCameraActivity", "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        lastImageUri = output.getSavedUri();
                        runOnUiThread(() -> {
                            thumbnailView.setImageURI(lastImageUri);
                            thumbnailView.setVisibility(View.VISIBLE);
                            Toast.makeText(OpenCameraActivity.this, "Image captured", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
