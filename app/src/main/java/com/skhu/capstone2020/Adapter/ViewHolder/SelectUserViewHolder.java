package com.skhu.capstone2020.Adapter.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.R;

public class SelectUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView select_user_photo;
    public TextView select_user_name;
    public ImageView select_user_check;
    public RecyclerItemClickListener recyclerItemClickListener;

    public SelectUserViewHolder(@NonNull View itemView) {
        super(itemView);
        select_user_name = itemView.findViewById(R.id.select_user_name);
        select_user_check = itemView.findViewById(R.id.select_user_check);
        select_user_photo = itemView.findViewById(R.id.select_user_photo);
        select_user_photo.setClipToOutline(true);
        itemView.setOnClickListener(this);
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    @Override
    public void onClick(View view) {
        recyclerItemClickListener.onItemClickListener(view, getAdapterPosition());
    }
}
