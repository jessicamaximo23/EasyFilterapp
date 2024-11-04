package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private TextView resetPasswordButton, signUpButton ;
    private Button signInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.cirLoginButton);
        signUpButton = findViewById(R.id.signUpButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        signInButton.setOnClickListener(view -> signInUser());

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInScreen.this, SignUpScreen.class);
            startActivity(intent);
        });

        // Redireciona para a tela de redefinição de senha
        resetPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInScreen.this, ResetScreen.class);
            startActivity(intent);
        });

    }

    private void signInUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInScreen.this, "Please, insert email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            Toast.makeText(SignInScreen.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            // Bloco try-catch para abrir o AdminPanelActivity
                            try {
                                if (email.equals("jessicamaximo23@gmail.com")) {
                                    startActivity(new Intent(SignInScreen.this, AdminPanelActivity.class));
                                } else {
                                    startActivity(new Intent(SignInScreen.this, MainActivity.class));
                                }

                                finish(); // Finaliza a SignInScreen após abrir a tela correta
                            } catch (Exception e) {
                                Toast.makeText(SignInScreen.this, "Error opening Admin Panel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignInScreen.this, "User is null after login", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(SignInScreen.this, "Try again: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}