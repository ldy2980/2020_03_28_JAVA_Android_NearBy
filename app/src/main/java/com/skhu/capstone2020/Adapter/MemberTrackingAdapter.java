package com.skhu.capstone2020.Adapter;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.UserOptions;
import com.skhu.capstone2020.R;

import java.util.List;

public class MemberTrackingAdapter extends RecyclerView.Adapter<MemberTrackingAdapter.ViewHolder> {
    private Context context;
    private GroupInfo groupInfo;
    private List<Member> memberList;

    private CollectionReference userOptionReference = FirebaseFirestore.getInstance().collection("UserOptions");

    public MemberTrackingAdapter(Context context, GroupInfo groupInfo) {
        this.context = context;
        this.groupInfo = groupInfo;
        this.memberList = groupInfo.getMemberList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_tracking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (memberList.size() != 0) {
            Member member = memberList.get(position);
            holder.member_tracking_name.setText(member.getName());

            if (!member.getImageUrl().equals("default")) {
                holder.member_tracking_photo.setPadding(0, 0, 0, 0);
                Glide.with(context)
                        .load(member.getImageUrl())
                        .into(holder.member_tracking_photo);
            }

            userOptionReference.document(member.getId())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                UserOptions options = documentSnapshot.toObject(UserOptions.class);

                                if (options != null)
                                    if (options.isAllowShareLocation()) {   // 위치 공유 설정 확인
                                        holder.member_tracking_no_tracking.setVisibility(View.INVISIBLE);
                                        holder.member_tracking_distance.setVisibility(View.VISIBLE);
                                        holder.member_tracking_time.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.member_tracking_distance.setVisibility(View.INVISIBLE);
                                        holder.member_tracking_no_tracking.setVisibility(View.VISIBLE);
                                        holder.member_tracking_time.setVisibility(View.INVISIBLE);
                                    }
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView member_tracking_photo, member_tracking_no_tracking;
        public TextView member_tracking_name, member_tracking_time, member_tracking_distance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            member_tracking_no_tracking = itemView.findViewById(R.id.member_tracking_no_tracking);
            member_tracking_distance = itemView.findViewById(R.id.member_tracking_distance);
            member_tracking_name = itemView.findViewById(R.id.member_tracking_name);
            member_tracking_time = itemView.findViewById(R.id.member_tracking_time);
            member_tracking_photo = itemView.findViewById(R.id.member_tracking_photo);
            member_tracking_photo.setClipToOutline(true);
        }
    }
}
