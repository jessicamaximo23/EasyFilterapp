package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private TextView resetPasswordButton, signUpButton;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        TextInputLayout textInputPassword = findViewById(R.id.textInputPassword);
        signInButton = findViewById(R.id.cirLoginButton);
        signUpButton = findViewById(R.id.signUpButton);

        //Icon visibility password
        textInputPassword.setEndIconOnClickListener(v -> {
            if (editTextPassword.getTransformationMethod() instanceof HideReturnsTransformationMethod) {

                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                // Se a senha estiver oculta, mostramos ela
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }

            // Atualiza o cursor para o final do texto, mantendo o foco no local certo
            editTextPassword.setSelection(editTextPassword.getText().length());
        });


        TextView resetPasswordButton = findViewById(R.id.resetPasswordButton);
        if (resetPasswordButton != null) {
            resetPasswordButton.setText("Forgot Password? Click here ");
        } else {
            Log.e("SignInScreen", "!");
        }


        signInButton.setOnClickListener(view -> signInUser());

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInScreen.this, SignUpScreen.class);
            startActivity(intent);
        });

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
                            DatabaseReference usersRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(user.getUid());

                            // Verifica se o usuário está bloqueado
                            usersRef.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful() && task1.getResult().exists()) {
                                    Boolean isBlocked = task1.getResult().child("isBlocked")
                                            .getValue(Boolean.class);

                                    if (Boolean.TRUE.equals(isBlocked)) {
                                        mAuth.signOut();
                                        Toast.makeText(SignInScreen.this, "Your account is blocked. Please contact support.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(SignInScreen.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        // Verifica se é admin ou usuário comum
                                        navigateToNextScreen(email);
                                    }
                                } else {
                                    Toast.makeText(SignInScreen.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(SignInScreen.this, "User is null after login", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(SignInScreen.this, "Password or Email wrong", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToNextScreen(String email) {
        try {
            if (email.equals("jessicamaximo23@gmail.com")) {
                startActivity(new Intent(SignInScreen.this, AdminPanelActivity.class));
            } else {
                startActivity(new Intent(SignInScreen.this, MainActivity.class));
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(SignInScreen.this, "Error opening Admin Panel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
