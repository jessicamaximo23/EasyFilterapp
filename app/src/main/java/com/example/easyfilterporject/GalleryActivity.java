package com.example.easyfilterporject;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private LinearLayout imageContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);
//        imageContainer = findViewById(R.id.imageContainer);

        // Chama o método para abrir a galeria com seleção múltipla
        openGallery();

    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permite seleção múltipla
        startActivityForResult(Intent.createChooser(intent, "Selecione Imagens"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            // Limpa o container para novas imagens selecionadas
            imageContainer.removeAllViews();

            if (data.getClipData() != null) {
                // Múltiplas imagens selecionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addImageToContainer(imageUri);
                }
            } else if (data.getData() != null) {
                // Apenas uma imagem selecionada
                Uri imageUri = data.getData();
                addImageToContainer(imageUri);
            }
        }
    }

    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300 // Altura fixa para cada imagem
        ));
        imageView.setImageURI(imageUri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imageContainer.addView(imageView);
    }
}
