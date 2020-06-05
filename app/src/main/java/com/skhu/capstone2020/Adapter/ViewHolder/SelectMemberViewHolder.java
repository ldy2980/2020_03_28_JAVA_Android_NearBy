package com.skhu.capstone2020.Adapter.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.R;

public class SelectMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView select_member_image;
    public TextView select_member_name, select_member_time, select_member_distance;
    public RecyclerItemClickListener recyclerItemClickListener;

    public SelectMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        select_member_name = itemView.findViewById(R.id.select_member_name);
        select_member_time = itemView.findViewById(R.id.select_member_time);
        select_member_distance = itemView.findViewById(R.id.select_member_distance);
        select_member_image = itemView.findViewById(R.id.select_member_image);
        select_member_image.setClipToOutline(true);
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
