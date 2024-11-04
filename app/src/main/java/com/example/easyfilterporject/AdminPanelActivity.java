package com.example.easyfilterporject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private DatabaseReference usersRef;
    private ArrayList<user> userList;
    private UsersAdapter usersAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_panel);
        recyclerViewUsers = findViewById(R.id.recyclerViewusers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        usersRef = FirebaseDatabase.getInstance().getReference("users"); // Path to users in Firebase
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList);
        recyclerViewUsers.setAdapter(usersAdapter);

        loadUsers();

    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    user user = userSnapshot.getValue(user.class);
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

    private class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
        private final ArrayList<user> users;

        UsersAdapter(ArrayList<user> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

      

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            user user = users.get(position);
            holder.textViewUserEmail.setText(user.getEmail());

            holder.buttonDeleteUser.setOnClickListener(v -> deleteUser(user.getId()));

            holder.buttonEditUser.setOnClickListener(v -> showEditDialog(user));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        private class UserViewHolder extends RecyclerView.ViewHolder {
            TextView textViewUserEmail;
            Button buttonEditUser, buttonDeleteUser;

            UserViewHolder(View itemView) {
                super(itemView);
                textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
                buttonEditUser = itemView.findViewById(R.id.buttonEditUser);
                buttonDeleteUser = itemView.findViewById(R.id.buttonDeleteUser);
            }
        }
    }

    private void deleteUser(String userId) {
        usersRef.child(userId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminPanelActivity.this, "User deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminPanelActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog(user user) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText editTextNewEmail = new EditText(this);
        editTextNewEmail.setText(user.getEmail());

        dialogBuilder.setTitle("Edit Email")
                .setView(editTextNewEmail)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newEmail = editTextNewEmail.getText().toString();
                    updateUserEmail(user.getId(), newEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserEmail(String userId, String newEmail) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);
        usersRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminPanelActivity.this, "Email updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminPanelActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show());
    }
}
