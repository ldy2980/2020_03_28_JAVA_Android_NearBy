package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.R;

import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {
    private List<User> friendList;
    private Context mContext;

    FriendsListAdapter() {
    }

    public FriendsListAdapter(List<User> friendList, Context mContext) {
        this.friendList = friendList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (friendList.size() != 0) {
            User user = friendList.get(position);
            holder.friend_name.setText(user.getName());
            holder.friend_status_message.setText(user.getStatusMessage());
            if (!(user.getImageUrl().equals("default")))
                Glide.with(mContext).load(user.getImageUrl()).into(holder.friend_photo);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView friend_name, friend_status_message;
        ImageView friend_photo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            friend_name = itemView.findViewById(R.id.friend_name);
            friend_status_message = itemView.findViewById(R.id.friend_status_message);
            friend_photo = itemView.findViewById(R.id.friend_photo);
        }
    }
}
