package com.example.easyfilterporject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AdminPanelActivity extends AppCompatActivity  {

    private RecyclerView recyclerViewUsers;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private ArrayList<User> userList;
    private UsersAdapter usersAdapter;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null || !currentUser.getEmail().equals("jessicamaximo23@gmail.com")) {
            Toast.makeText(this, "Access Denied: Admin Only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userList = new ArrayList<>();

        // Passando um OnUserClickListener para o UsersAdapter
        usersAdapter = new UsersAdapter(userList, new UsersAdapter.OnUserClickListener() {
            @Override
            public void onEditClick(User user) {
                showEditDialog(user);
            }

            @Override
            public void onDeleteClick(String userId) {
                deleteUser(userId);
            }
        });

        recyclerViewUsers.setAdapter(usersAdapter);
        loadUsers();

        buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminPanelActivity.this, SignInScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }


    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userSnapshot.getKey());
                        userList.add(user);

                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPanelActivity.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

         if (currentUser != null && currentUser.getEmail().equals("jessicamaximo23@gmail.com")) {
            // O administrador pode excluir qualquer usuário
            usersRef.child(userId).removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(AdminPanelActivity.this, "User deleted from Database", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(AdminPanelActivity.this, "Failed to delete user from Database", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(AdminPanelActivity.this, "Access Denied: Admin Only", Toast.LENGTH_SHORT).show();
        }
    }
    private void showEditDialog(User user) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText editTextNewEmail = new EditText(this);
        editTextNewEmail.setText(user.getEmail());

        dialogBuilder.setTitle("Edit Email")
                .setView(editTextNewEmail)
                .setPositiveButton("Save", (dialog, which) -> {

                    String newEmail = editTextNewEmail.getText().toString().trim();
                    if (!newEmail.isEmpty()) {
                        updateUserEmail(user.getId(), newEmail);
                    } else {
                        Toast.makeText(AdminPanelActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserEmail(String userId, String newEmail) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getEmail().equals("jessicamaximo23@gmail.com")) {
            // O administrador pode editar o e-mail de qualquer usuário
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("email", newEmail);
            usersRef.child(userId).updateChildren(updates)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(AdminPanelActivity.this, "Email updated in Database", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(AdminPanelActivity.this, "Failed to update email in Database", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(AdminPanelActivity.this, "Access Denied: Admin Only", Toast.LENGTH_SHORT).show();
        }
    }
}
