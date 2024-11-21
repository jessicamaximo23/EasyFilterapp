package com.example.easyfilterporject;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
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
    private Uri lastImageUri;
    private boolean isFrontCamera = false;  // Alternar entre câmera frontal e traseira
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ImageView imageViewGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        // Inicializa as views
        previewView = findViewById(R.id.preview_view);
        imageViewGallery = findViewById(R.id.imageViewGallery);

        // Verifica permissão da câmera e inicializa a câmera
        if (checkCameraPermission()) {
            startCamera();
        }
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

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtem a rotação do dispositivo no momento
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        imageCapture.setTargetRotation(rotation);

        // Cria o arquivo onde a imagem será salva
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "photo_" + System.currentTimeMillis() + ".jpg");

        // Opções de saída para salvar a imagem
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Captura a foto
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(OpenCameraActivity.this, "Photo saved: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                        // Atualiza a galeria com a imagem salva
                        addPhotoToGallery(photoFile);

                        // Caso queira exibir a imagem capturada:
                        displayCapturedPhoto(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(OpenCameraActivity.this, "Failed to capture photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
        );
    }


    private void addPhotoToGallery(File photoFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photoFile);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void displayCapturedPhoto(File photoFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        // Corrige a rotação da imagem (se necessário)
        Bitmap correctedBitmap = correctImageRotation(photoFile.getAbsolutePath());

        // Exibe a imagem corrigida no ImageView
        imageViewGallery.setImageBitmap(correctedBitmap);
    }

    private Bitmap correctImageRotation(String photoPath) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
            return bitmap; // Retorna o bitmap original se ocorrer um erro
        }
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

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Obtém o CameraProvider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Cria a pré-visualização com configurações
                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Ajusta para o aspecto desejado
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()) // Define a rotação
                        .build();

                // Configura o seletor de câmera (frontal ou traseira)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isFrontCamera ?
                                CameraSelector.LENS_FACING_FRONT :
                                CameraSelector.LENS_FACING_BACK)
                        .build();

                // Configura o capturador de imagens
                imageCapture = new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Ajusta para o mesmo aspecto do preview
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()) // Define a rotação
                        .build();

                // Vincula a câmera ao ciclo de vida, ao Preview e ao ImageCapture
                cameraProvider.unbindAll(); // Certifica-se de não haver vinculações anteriores
                Camera camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

                // Fornece o surface provider para exibir a pré-visualização no PreviewView
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error initializing camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
