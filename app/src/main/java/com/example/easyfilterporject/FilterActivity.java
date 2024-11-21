package com.example.easyfilterporject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
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
    private Button applyFilterButton;
    private Bitmap originalBitmap;
    private GPUImage gpuImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Inicialização dos elementos de UI
        filteredImageView = findViewById(R.id.filteredImageView);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);
        applyFilterButton = findViewById(R.id.applyFilterButton);

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

            applyFilterButton.setOnClickListener(v -> saveAndReturn());
        } else {
            // Caso o caminho seja nulo, informa o usuário e fecha a Activity
            Toast.makeText(this, "Image path is missing", Toast.LENGTH_SHORT).show();
            finish();
        }


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
        filterGroup.addFilter(new GPUImageBrightnessFilter(brightness));
        filterGroup.addFilter(new GPUImageContrastFilter(contrast));

        if (gpuImage != null) {
            gpuImage.setFilter(filterGroup);
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    //if the user want to reset the image
    private void resetToOriginal() {
        if (gpuImage != null) {
            gpuImage.setImage(originalBitmap);
            filteredImageView.setImageBitmap(originalBitmap);
        }
    }

    private void saveAndReturn() {
        if (gpuImage != null) {
            Bitmap resultBitmap = gpuImage.getBitmapWithFilterApplied();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("filteredBitmapPath", saveBitmapToCache(resultBitmap));
            setResult(RESULT_OK, resultIntent);
            finish();
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

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageGrayscaleFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    private void applySepiaFilter() {

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageSepiaToneFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }
    }

    private void applyNegativeFilter() {

        if (gpuImage != null) {
            gpuImage.setFilter(new GPUImageColorInvertFilter());
            filteredImageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }


    }
}