package com.example.easyfilterporject;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;


import java.io.IOException;

public class FilterActivity extends AppCompatActivity {

    private ImageView imageView;
    private GPUImageView gpuImageView;
    private Bitmap selectedImage;
    private Uri imageUri;
    private SeekBar brightnessSeekBar, contrastSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        imageView = findViewById(R.id.imageView);
        gpuImageView = findViewById(R.id.gpuImageView);
//        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
//        contrastSeekBar = findViewById(R.id.contrastSeekBar);

        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));

        try {
            selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            gpuImageView.setImage(selectedImage); // Define a imagem no GPUImageView
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Botões para aplicar filtros
        findViewById(R.id.btnSepia).setOnClickListener(v -> applySepiaFilter());
        findViewById(R.id.btnInvert).setOnClickListener(v -> applyInvertFilter());
        findViewById(R.id.btnGrayscale).setOnClickListener(v -> applyGrayscaleFilter());
        findViewById(R.id.btnCustomFilter).setOnClickListener(v -> applyCustomFilter());

        // Configurar SeekBars
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                applyBrightnessFilter(progress - 100);  // Ajuste para o valor do seekBar
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                applyContrastFilter(progress - 100);  // Ajuste para o valor do seekBar
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Método para aplicar o filtro Sepia
    private void applySepiaFilter() {
        gpuImageView.setFilter(new GPUImageSepiaToneFilter());
    }

    // Método para aplicar o filtro de inversão de cores
    private void applyInvertFilter() {
        gpuImageView.setFilter(new GPUImageColorInvertFilter());
    }

    // Método para aplicar o filtro de escala de cinza
    private void applyGrayscaleFilter() {
        gpuImageView.setFilter(new GPUImageGrayscaleFilter());
    }

    // Método para aplicar um filtro customizado
    private void applyCustomFilter() {
        // Exemplo de filtro customizado
        GPUImageFilter customFilter = new GPUImageFilter() {
            @Override
            public void onDrawArraysPre() {
                super.onDrawArraysPre();
                // Customização do filtro
            }
        };
        gpuImageView.setFilter(customFilter);
    }

    // Método para aplicar filtro de brilho
    private void applyBrightnessFilter(int brightness) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        imageView.setColorFilter(filter);
    }

    // Método para aplicar filtro de contraste
    private void applyContrastFilter(int contrast) {
        ColorMatrix colorMatrix = new ColorMatrix();
        float scale = contrast / 100f + 1;
        float translate = 128 * (1 - scale);

        colorMatrix.set(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        imageView.setColorFilter(filter);
    }
}
