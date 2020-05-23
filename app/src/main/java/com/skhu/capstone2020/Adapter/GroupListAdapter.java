package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skhu.capstone2020.GroupActivity;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.R;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private List<GroupInfo> groupInfoList;
    private Context context;

    public GroupListAdapter(List<GroupInfo> groupInfoList, Context context) {
        this.groupInfoList = groupInfoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (groupInfoList.size() != 0) {
            GroupInfo groupInfo = groupInfoList.get(position);
            holder.group_name.setText(groupInfo.getGroupName());
            holder.group_count.setTextColor(groupInfo.getCount());
            holder.member_last_message.setText(groupInfo.getLastMessage());
            holder.member_last_message_time.setText(groupInfo.getLastMessageTime());

            List<Member> memberList = groupInfo.getMemberList();
            List<ImageView> imageViewList = holder.getImageViewList();
            for (int i = 0; i < memberList.size(); ++i) {
                if (i > 3)
                    return;
                if (!(memberList.get(i).getImageUrl().equals("default"))) {
                    imageViewList.get(i).setPadding(0, 0, 0, 0);
                    Glide.with(context)
                            .load(memberList.get(i).getImageUrl())
                            .into(imageViewList.get(i));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView group_name, member_last_message, member_last_message_time, group_count;
        ImageView member_image1, member_image2, member_image3, member_image4;
        List<ImageView> imageViewList = new ArrayList<>();

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            group_name = itemView.findViewById(R.id.group_name);
            member_last_message = itemView.findViewById(R.id.member_last_message);
            member_last_message_time = itemView.findViewById(R.id.member_last_message_time);
            group_count = itemView.findViewById(R.id.group_count);
            itemView.setOnClickListener(this);
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

        List<ImageView> getImageViewList() {
            return imageViewList;
        }

        @Override
        public void onClick(View view) {                                                            // 클릭 시 그룹 액티비티로 이동
            GroupInfo groupInfo = groupInfoList.get(getAdapterPosition());
            Intent intent = new Intent(context, GroupActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            view.getContext().startActivity(intent);
        }
    }
}
