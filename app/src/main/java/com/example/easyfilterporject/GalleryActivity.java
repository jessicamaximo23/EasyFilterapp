package com.example.easyfilterporject;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);
        imageView = findViewById(R.id.imageView);

        // Chama o método para abrir a galeria com seleção múltipla
        openGallery();

    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //Convert this URI image for BITMAP
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                // Load the image  for gallery
                Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                // Aplicar filtro simples (exemplo: converter para tons de cinza)
                Bitmap filteredBitmap = applyGrayScaleFilter(selectedImageBitmap);

                // Show the image ImageView
                imageView.setImageBitmap(filteredBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Bitmap applyGrayScaleFilter(Bitmap original) {
        Bitmap grayScaleBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(grayScaleBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(original, 0, 0, paint);
        return grayScaleBitmap;
    }
}

