package com.skhu.capstone2020;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.UserOptions;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<Member> memberList;
    private Context context;
    private GroupInfo groupInfo;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public MemberAdapter(List<Member> memberList, GroupInfo groupInfo, Context context) {
        this.memberList = memberList;
        this.groupInfo = groupInfo;
        this.context = context;

        for (int i = 0; i < memberList.size(); ++i)
            if (currentUser.getUid().equals(memberList.get(i).getId())) {
                memberList.remove(i);
                return;
            }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (memberList.size() != 0) {
            Member member = memberList.get(position);
            holder.member_name.setText(member.getName());

            if (!member.getImageUrl().equals("default")) {
                holder.member_photo.setPadding(0, 0, 0, 0);
                Glide.with(context)
                        .load(member.getImageUrl())
                        .into(holder.member_photo);
            }

            if (member.getId().equals(groupInfo.getMasterId()))                                     // 마스터일 경우 아이콘 표시
                holder.member_master.setVisibility(View.VISIBLE);

            FirebaseFirestore.getInstance()
                    .collection("UserOptions")
                    .document(member.getId())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                UserOptions userOptions = documentSnapshot.toObject(UserOptions.class);
                                if (userOptions != null && !(userOptions.isAllowShareLocation()))
                                    holder.member_no_tracking.setVisibility(View.VISIBLE);          // 위치 공유 옵션 OFF시 아이콘 표시
                                else
                                    holder.member_no_tracking.setVisibility(View.INVISIBLE);        //      ''       ON시 아이콘 제거
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView member_name, member_address;
        ImageView member_photo, member_master, member_no_tracking;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            member_name = itemView.findViewById(R.id.member_name);
            member_address = itemView.findViewById(R.id.member_address);
            member_master = itemView.findViewById(R.id.member_master);
            member_no_tracking = itemView.findViewById(R.id.member_no_tracking);
            member_photo = itemView.findViewById(R.id.member_photo);
            member_photo.setClipToOutline(true);
        }
    }
}
