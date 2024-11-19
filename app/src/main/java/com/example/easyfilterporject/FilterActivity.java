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
import android.widget.Button;
import android.widget.Toast;

public class FilterActivity extends AppCompatActivity {

    private ImageView filteredImageView;
    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;
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

                // Aplica filtros iniciais ao carregar a imagem
                applyInitialFilters();

                // Define o listener para o botão de aplicar filtro
                applyFilterButton.setOnClickListener(v -> applyFilters());

                // Ajuste dinâmico de brilho
                brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        applyFilters();
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
            } else {
                // Caso a imagem não tenha sido carregada corretamente
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Caso o caminho da imagem não tenha sido passado
            Toast.makeText(this, "Image path is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Aplica filtros iniciais ao carregar a imagem
    private void applyInitialFilters() {
        if (gpuImage != null) {
            // Aplicando filtro de brilho e contraste padrão
            applyFilters();
        }
    }

    private void applyFilters() {
        if (gpuImage != null) {
            // Obtém o valor dos SeekBars
            float brightness = (brightnessSeekBar.getProgress() - 100) / 100.0f; // De -1 a 1
            float contrast = contrastSeekBar.getProgress() / 100.0f; // De 0 a 2

            // Aplicando filtro de brilho
            GPUImageBrightnessFilter brightnessFilter = new GPUImageBrightnessFilter(brightness);
            gpuImage.setFilter(brightnessFilter);

            // Aplicando filtro de contraste
            GPUImageContrastFilter contrastFilter = new GPUImageContrastFilter(contrast);
            gpuImage.setFilter(contrastFilter);

            // Aplicando a imagem filtrada
            Bitmap filteredBitmap = gpuImage.getBitmapWithFilterApplied();
            filteredImageView.setImageBitmap(filteredBitmap);

            // Enviar a imagem filtrada de volta para a MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("filteredBitmap", filteredBitmap);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}