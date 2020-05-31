package com.skhu.capstone2020.Adapter.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.R;

import java.util.ArrayList;
import java.util.List;

public class SelectGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView group_name, member_last_message, member_last_message_time, group_count;
    public ImageView member_image1, member_image2, member_image3, member_image4;
    public List<ImageView> imageViewList = new ArrayList<>();

    public RecyclerItemClickListener recyclerItemClickListener;

    public SelectGroupViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        group_name = itemView.findViewById(R.id.group_name);
        member_last_message = itemView.findViewById(R.id.member_last_message);
        member_last_message_time = itemView.findViewById(R.id.member_last_message_time);
        group_count = itemView.findViewById(R.id.group_count);
        member_image1 = itemView.findViewById(R.id.member_image1);
        member_image2 = itemView.findViewById(R.id.member_image2);
        member_image3 = itemView.findViewById(R.id.member_image3);
        member_image4 = itemView.findViewById(R.id.member_image4);
        member_image1.setClipToOutline(true);
        member_image2.setClipToOutline(true);
        member_image3.setClipToOutline(true);
        member_image4.setClipToOutline(true);
        imageViewList.add(member_image1);
        imageViewList.add(member_image2);
        imageViewList.add(member_image3);
        imageViewList.add(member_image4);
    }

    public List<ImageView> getImageViewList() {
        return imageViewList;
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    @Override
    public void onClick(View view) {
        recyclerItemClickListener.onItemClickListener(view, getAdapterPosition());
    }
}
