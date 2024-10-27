package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Temporizador para a splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Navega para a MainActivity após 2 segundos
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000); // Tempo de exibição da splash screen (2 segundos)
    }
}