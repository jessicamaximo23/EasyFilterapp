package com.example.easyfilterporject;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;


import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;

import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FilterActivity extends AppCompatActivity {

    private ImageView filteredImageView;
    private SeekBar brightnessSeekBar, contrastSeekBar;
    private Bitmap originalBitmap;
    private GPUImage gpuImage;
    private GPUImageFilter activeFilter = null;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        ImageView iconViewFilters = findViewById(R.id.iconViewFilters);
        LinearLayout filterButtonsContainer = findViewById(R.id.filterButtonsContainer);
        ImageView iconSavePhoto = findViewById(R.id.iconSavePhoto);


        filteredImageView = findViewById(R.id.filteredImageView);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);


        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath != null) {

            originalBitmap = BitmapFactory.decodeFile(imagePath);

            if (originalBitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            gpuImage = new GPUImage(this);
            gpuImage.setImage(originalBitmap);
            filteredImageView.setImageBitmap(originalBitmap);


        } else {
            // Caso o caminho seja nulo, informa o usuário e fecha a Activity
            Toast.makeText(this, "Image path is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        iconViewFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alternar a visibilidade dos botões de filtro
                if (filterButtonsContainer.getVisibility() == View.GONE) {
                    filterButtonsContainer.setVisibility(View.VISIBLE);
                } else {
                    filterButtonsContainer.setVisibility(View.GONE);
                }
            }
        });

        // Configurar botões de filtro
        findViewById(R.id.btnFilterGrayScale).setOnClickListener(v -> applyGrayScaleFilter());
        findViewById(R.id.btnFilterSepia).setOnClickListener(v -> applySepiaFilter());
        findViewById(R.id.btnFilterNegative).setOnClickListener(v -> applyNegativeFilter());
        findViewById(R.id.btnFilterOriginal).setOnClickListener(v -> resetToOriginal());


        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateBrightnessAndContrast();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateBrightnessAndContrast();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //After apply filter save the photo on my cell
        iconSavePhoto.setOnClickListener(v -> savePhotoToGalleryAndFirebase());


        FirebaseApp.initializeApp(this);

    }

    private void applyGPUFilter(GPUImageFilter filter) {
        if (gpuImage != null) {
            gpuImage.setFilter(filter);
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    private void updateBrightnessAndContrast() {

        float brightness = (brightnessSeekBar.getProgress() - 100) / 100.0f;
        float contrast = contrastSeekBar.getProgress() / 100.0f;

        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();

        //put the option for user add one filter + contrast and bright
        if (activeFilter != null) {
            filterGroup.addFilter(activeFilter);
        }

        filterGroup.addFilter(new GPUImageBrightnessFilter(brightness));
        filterGroup.addFilter(new GPUImageContrastFilter(contrast));

        if (gpuImage != null) {
            gpuImage.setFilter(filterGroup);
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    //if the user want to reset the image
    private void resetToOriginal() {
        activeFilter = null;

        if (gpuImage != null) {
            gpuImage.setImage(originalBitmap);
            filteredImageView.setImageBitmap(originalBitmap);
        }
    }

    private void saveAndReturn() {
        if (gpuImage != null) {

            Bitmap resultBitmap = gpuImage.getBitmapWithFilterApplied();

            // Salva o Bitmap no cache e retorna o caminho do arquivo
            String savedImagePath = saveBitmapToCache(resultBitmap);

            if (savedImagePath != null) {
                Toast.makeText(this, "Image saved at: " + savedImagePath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save the filtered image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveBitmapToCache(Bitmap bitmap) {
        try {
            File cacheDir = getCacheDir();
            File file = new File(cacheDir, "filtered_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            return file.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applyGrayScaleFilter() {

        applyGPUFilter(new GPUImageGrayscaleFilter());

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageGrayscaleFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    private void applySepiaFilter() {

        applyGPUFilter(new GPUImageSepiaToneFilter());

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageSepiaToneFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    private void applyNegativeFilter() {

        applyGPUFilter(new GPUImageColorInvertFilter());

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageColorInvertFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                savePhotoToGallery();
            } else {

                Toast.makeText(this, "Permission denied to write external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //save the photo on my cel
    private void savePhotoToGallery() {
        if (gpuImage == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap resultBitmap = gpuImage.getBitmapWithFilterApplied();

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

    //save on firestore
    private void uploadImageToFirebaseStorage(Bitmap resultBitmap) {

        if (resultBitmap == null) {
            Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        String userUid = currentUser.getUid();
        String imagePath = "photos/" + userEmail + "/" + "filtered_" + System.currentTimeMillis() + ".png";
        // Converter a imagem para byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload para Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(imagePath);
        storageRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Passe os argumentos corretos para o método saveImageDataToDatabase
                    saveImageDataToDatabase(uri.toString(), userEmail, userUid);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void saveImageDataToDatabase(String imageUrl, String email, String userUid) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("user_images");

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", imageUrl);
        imageData.put("email", email);
        imageData.put("timestamp", System.currentTimeMillis());

        databaseRef.push().setValue(imageData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Image data saved to Database", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save image data", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });

    }


    private void savePhotoToGalleryAndFirebase() {

        if (gpuImage == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap resultBitmap = gpuImage.getBitmapWithFilterApplied();

        try {
            // Salvar na galeria
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "EasyFilterPhotos");
            if (!appDir.exists()) appDir.mkdirs();

            File imageFile = new File(appDir, "filtered_" + System.currentTimeMillis() + ".png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            // Atualizar galeria
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(imageFile));
            sendBroadcast(mediaScanIntent);

            Toast.makeText(this, "Saved to Gallery!", Toast.LENGTH_SHORT).show();

            uploadBitmapToFirebaseStorage(resultBitmap);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadBitmapToFirebaseStorage(Bitmap resultBitmap) {

        if (resultBitmap == null) {
            Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        String userUid = currentUser.getUid();
        String imagePath = "photos/" + userEmail + "/" + "filtered_" + System.currentTimeMillis() + ".png";

        // Converter a imagem para byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload para Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(imagePath);
        storageRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveImageDataToDatabase(uri.toString(), userEmail, userUid);

                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}

