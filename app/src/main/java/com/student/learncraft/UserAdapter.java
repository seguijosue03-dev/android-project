package com.student.learncraft;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the layout file you provided (item_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // 1. Set Data to Views (Matching your XML IDs)
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole()); // Assuming User class has getRole()

        // If your User class has a date field, set it here. Otherwise, static text:
        // holder.tvDate.setText(user.getDateRegistered());

        // 2. CLICK LISTENER - Opens History (This is what you need!)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserQuizzesActivity.class);
            intent.putExtra("user_email", user.getEmail());
            intent.putExtra("user_name", user.getFullName());
            context.startActivity(intent);
        });

        // 3. DELETE BUTTON LISTENER
        holder.btnDelete.setOnClickListener(v -> {
            // For now, just show a message. You can add delete logic later.
            Toast.makeText(context, "Delete clicked for: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare all the views from your XML
        TextView tvName, tvEmail, tvRole, tvDate;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Connect IDs from the XML you provided
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvDate = itemView.findViewById(R.id.tvUserDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}