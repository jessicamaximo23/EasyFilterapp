package com.example.easyfilterporject;
mport android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
    private boolean isFrontCamera = false;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        // Inicializar views
        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);
        switchCameraButton = findViewById(R.id.switch_camera);
        backButton = findViewById(R.id.back_button);
        thumbnailView = findViewById(R.id.thumbnail_view);

        // Verificar permissão da câmera
        if (checkCameraPermission()) {
            startCamera();
        }

        // Configurar listeners dos botões
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        captureButton.setOnClickListener(v -> takePhoto());

        switchCameraButton.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;
            startCamera();
        });

        backButton.setOnClickListener(v -> {
            if (lastImageUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.setData(lastImageUri);
                setResult(RESULT_OK, resultIntent);
            }
            finish();
        });

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
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Configurar o preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Configurar a captura de imagem
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                // Selecionar câmera frontal ou traseira
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isFrontCamera ?
                                CameraSelector.LENS_FACING_FRONT :
                                CameraSelector.LENS_FACING_BACK)
                        .build();

                // Vincular casos de uso à câmera
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector,
                        preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera",
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // Criar arquivo para a foto
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating image file",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar o output da foto
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Tirar a foto
        imageCapture.takePicture(outputFileOptions, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        lastImageUri = FileProvider.getUriForFile(
                                OpenCameraActivity.this,
                                "com.example.easyfilterporject.fileprovider",
                                photoFile);

                        runOnUiThread(() -> {
                            thumbnailView.setImageURI(lastImageUri);
                            thumbnailView.setVisibility(View.VISIBLE);
                            Toast.makeText(OpenCameraActivity.this,
                                    "Image captured", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        runOnUiThread(() -> Toast.makeText(OpenCameraActivity.this,
                                "Error capturing image", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}