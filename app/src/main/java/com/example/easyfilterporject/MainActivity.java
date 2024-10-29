package com.example.easyfilterporject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //show the email adress
    private  FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();


        FirebaseUser currentUser = auth.getCurrentUser();
        TextView emailTextView = findViewById(R.id.textViewEmail);

        // Exibir o email se o usuário estiver conectado
        if (currentUser != null) {
            emailTextView.setText("Email: " + currentUser.getEmail());
        } else {
            emailTextView.setText("Nenhum usuário conectado");
        }

    }
}