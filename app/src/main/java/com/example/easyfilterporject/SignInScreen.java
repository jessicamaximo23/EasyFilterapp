package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button signInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.cirLoginButton);

        signInButton.setOnClickListener(view -> signInUser());

    }

    private void signInUser() {

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInScreen.this, "Please, insert your best email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido, redireciona para a página principal ou outra tela
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignInScreen.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignInScreen.this, MainActivity.class));
                        finish();
                    } else {
                        // Falha na autenticação, mostra mensagem de erro
                        Toast.makeText(SignInScreen.this, "Falha na autenticação: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}