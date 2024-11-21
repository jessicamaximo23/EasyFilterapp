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
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;

import android.widget.Button;
import android.widget.Toast;

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
            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);

            if (originalBitmap != null) {
                // Configura a imagem com GPUImage
                gpuImage = new GPUImage(this);
                gpuImage.setImage(originalBitmap);
                filteredImageView.setImageBitmap(originalBitmap);


                // Define o listener para o botão de aplicar filtro
                applyFilterButton.setOnClickListener(v -> applyFilters());

            }
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
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // Ajuste dinâmico de contraste
                contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        applyFilters();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        applyFilterButton.setOnClickListener(v -> saveAndReturn());

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

        applyGPUFilter(filterGroup);
    }

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
        // Código para salvar o bitmap e retornar o caminho.
        return null; // Implementar método.
    }
}