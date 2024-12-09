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
            public void onBlockToggleClick(User user) {
                toggleBlockEmail(user.getId(), user.isBlocked());
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

    private void showEditDialog(User user) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText editTextName = new EditText(this);
        editTextName.setText(user.getName());

        dialogBuilder.setTitle("Edit Name")
                .setView(editTextName)
                .setPositiveButton("Save", (dialog, which) -> {

                    String name = editTextName.getText().toString().trim();

                    if (!name.isEmpty()) {
                        updateUserName(user.getId(), name);
                    } else {
                        Toast.makeText(AdminPanelActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserName(String userId, String newName) {

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", newName);

        usersRef.child(userId).updateChildren(updates)
          .addOnSuccessListener(aVoid -> {

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (user.getId().equals(userId)) {
                user.setName(newName);
                usersAdapter.notifyItemChanged(i);
                break;
            }
        }
            Toast.makeText(AdminPanelActivity.this, "User name updated", Toast.LENGTH_SHORT).show();
        })
                .addOnFailureListener(e -> {
                    // Caso ocorra algum erro ao atualizar no Firebase
                    Toast.makeText(AdminPanelActivity.this, "Failed to update name in Database", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleBlockEmail(String userId, boolean isBlocked) {

        HashMap<String, Object> updates = new HashMap<>();

        updates.put("isBlocked", !isBlocked);
        usersRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    String message = isBlocked ? "User unlocked" : "User blocked";
                    Toast.makeText(AdminPanelActivity.this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AdminPanelActivity.this, "Failed to update block status in Database", Toast.LENGTH_SHORT).show()
                );
    }

}
