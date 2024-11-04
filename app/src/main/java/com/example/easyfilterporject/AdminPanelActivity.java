package com.example.easyfilterporject;

import android.os.Bundle;
import android.util.Log;
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


public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private DatabaseReference usersRef;
    private ArrayList<User> userList;
    private UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        recyclerViewUsers = findViewById(R.id.recyclerViewusers);
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

                        Log.d("AdminPanelActivity", "Loaded user: " + user.getEmail());

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
        usersRef.child(userId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminPanelActivity.this, "User deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminPanelActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show());
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
        // Primeiro, atualize o e-mail no Realtime Database
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);
        usersRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Agora, atualize o e-mail na autenticação do Firebase
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && user.getUid().equals(userId)) {
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AdminPanelActivity.this, "Email updated in Authentication", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminPanelActivity.this, "Failed to update email in Authentication", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(AdminPanelActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AdminPanelActivity.this, "Failed to update email in database", Toast.LENGTH_SHORT).show());
    }
}
