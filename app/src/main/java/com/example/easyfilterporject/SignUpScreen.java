package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextName,editTextEmail, editTextPassword;
    private Button cirRegisterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_screen);

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextName = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        cirRegisterButton = findViewById(R.id.cirRegisterButton);

        cirRegisterButton.setOnClickListener(v -> createAccount());
    }
    private void createAccount() {
        String username = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUpScreen.this, "Please insert all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            String userId = user.getUid();

                            User newUser = new User(username, email);

                            usersRef.child(userId).setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpScreen.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUpScreen.this, SignInScreen.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpScreen.this, "Failed to save user: " , Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpScreen.this, "Registration failed: " , Toast.LENGTH_SHORT).show();
                    }
                });
    }
}