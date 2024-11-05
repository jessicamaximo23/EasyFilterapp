package com.example.easyfilterporject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final ArrayList<User> users;
    private final OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {

        void onEditClick(User user);
        void onDeleteClick(String userId);
    }

    public UsersAdapter(ArrayList<User> users, OnUserClickListener listener) {
        this.users = users;
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.textViewUserEmail.setText(user.getEmail());

        holder.buttonEditUser.setOnClickListener(v -> onUserClickListener.onEditClick(user));
        holder.buttonDeleteUser.setOnClickListener(v -> onUserClickListener.onDeleteClick(user.getId()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserEmail;
        Button buttonEditUser,buttonDeleteUser;

        UserViewHolder(View itemView) {
            super(itemView);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            buttonEditUser = itemView.findViewById(R.id.buttonEditUser);
            buttonDeleteUser = itemView.findViewById(R.id.buttonDeleteUser);
        }
    }
}
