package com.example.easyfilterporject;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

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

import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FilterActivity extends AppCompatActivity {

    private ImageView filteredImageView;
    private SeekBar brightnessSeekBar, contrastSeekBar;
    private Bitmap originalBitmap;
    private GPUImage gpuImage;
    private GPUImageFilter activeFilter = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        ImageView iconViewFilters = findViewById(R.id.iconViewFilters);
        LinearLayout filterButtonsContainer = findViewById(R.id.filterButtonsContainer);
        ImageView iconSavePhoto = findViewById(R.id.iconSavePhoto);



        // Inicialização dos elementos de UI
        filteredImageView = findViewById(R.id.filteredImageView);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);


        // Recuperar o caminho da imagem
        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath != null) {
            // Tenta decodificar a imagem a partir do caminho
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
                    filterButtonsContainer.setVisibility(View.VISIBLE); // Torna visível
                } else {
                    filterButtonsContainer.setVisibility(View.GONE); // Torna invisível
                }
            }
        });


        // Configurar botões de filtro
        findViewById(R.id.btnFilterGrayScale).setOnClickListener(v -> applyGrayScaleFilter());
        findViewById(R.id.btnFilterSepia).setOnClickListener(v -> applySepiaFilter());
        findViewById(R.id.btnFilterNegative).setOnClickListener(v -> applyNegativeFilter());
        findViewById(R.id.btnFilterOriginal).setOnClickListener(v -> resetToOriginal());

        // Ajustes de Brilho e Contraste
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
        // Ajuste dinâmico de contraste
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
        iconSavePhoto.setOnClickListener(v -> savePhotoToGallery());



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

        // Adiciona o filtro ativo, se houver
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
            return null; // Implementar método.
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
                // Permissão concedida, você pode continuar com o acesso ao armazenamento
                saveAndReturn();
            } else {
                // Permissão negada, você pode informar ao usuário
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

        if (resultBitmap == null) {
            Toast.makeText(this, "Failed to apply filter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica permissões para Android 6.0+ (API 23+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }

        // Salvar no armazenamento externo
        try {
            File picturesDir = new File(getExternalFilesDir(null), "EasyFilterPhotos");
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }

            File imageFile = new File(picturesDir, "filtered_" + System.currentTimeMillis() + ".png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Saved: " + imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


}

