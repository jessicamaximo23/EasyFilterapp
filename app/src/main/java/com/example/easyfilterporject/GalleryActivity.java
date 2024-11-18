package com.example.easyfilterporject;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
    private LinearLayout imageContainer;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        imageView = findViewById(R.id.imageView);
        imageContainer = findViewById(R.id.imageContainer);

        openGallery();

    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Converte a URI da imagem para Bitmap
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            // Abre o InputStream com o ContentResolver a partir da URI
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Decodifica o InputStream em um Bitmap
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Retorna null se houver erro ao carregar a imagem
        }
    }
    //Convert this URI image for BITMAP
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Crie um Bitmap da URI da imagem
                    Bitmap bitmap = getBitmapFromUri(selectedImageUri);

                    // Crie um novo ImageView dinamicamente
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(bitmap);

                    // Adiciona a ImageView ao LinearLayout
                    imageContainer.addView(imageView);
                }
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

