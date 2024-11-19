package com.example.easyfilterporject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;


public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2; // Código para identificar a ação da galeria
    private ImageView imageViewGallery; // Exibir a imagem na GalleryActivity
    private Uri selectedImageUri; // Armazenar a URI da imagem selecionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Inicializa o ImageView para exibir a imagem
        imageViewGallery = findViewById(R.id.imageViewGallery);

        // Abre a galeria assim que a atividade for iniciada
        openGallery();

        // Configura o clique para confirmar a imagem e voltar para a MainActivity
        findViewById(R.id.btnConfirmImage).setOnClickListener(v -> {
            if (selectedImageUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedImageUri", selectedImageUri.toString()); // Passa o URI da imagem para a MainActivity
                setResult(RESULT_OK, resultIntent); // Define o resultado OK para retornar à MainActivity
                finish(); // Finaliza a GalleryActivity
            } else {
                Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para abrir a galeria
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK); // Ação para escolher uma imagem
        intent.setType("image/*"); // Filtra para selecionar apenas imagens
        startActivityForResult(intent, PICK_IMAGE_REQUEST); // Inicia a galeria e espera o resultado
    }

    // Método que recebe o resultado da galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData(); // Recupera o URI da imagem selecionada

            if (selectedImageUri != null) {
                try {
                    // Converte o URI da imagem em Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageViewGallery.setImageBitmap(bitmap); // Exibe a imagem na GalleryActivity
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao carregar a imagem", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
