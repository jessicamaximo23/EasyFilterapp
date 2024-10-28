package com.example.easyfilterporject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
public class SignUpScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button cirRegisterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_screen);

        // Inicializa o FirebaseAuth
        mAuth = FirebaseAuth.getInstance();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        cirRegisterButton = findViewById(R.id.cirRegisterButton);

        // Configura o clique do botão de registro
        cirRegisterButton.setOnClickListener(v -> createAccount());
    }
    private void createAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validação dos campos de entrada
        if (email.isEmpty()) {
            Toast.makeText(SignUpScreen.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(SignUpScreen.this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar a conta com o Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro bem-sucedido
                        Toast.makeText(SignUpScreen.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        // Redirecionar para a tela de login ou a tela principal
                        // startActivity(new Intent(SignUpScreen.this, SignInScreen.class)); // Exemplo de redirecionamento
                        finish(); // Finaliza a atividade atual
                    } else {
                        // Falha no registro
                        Toast.makeText(SignUpScreen.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}