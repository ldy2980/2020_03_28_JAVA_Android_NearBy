package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.skhu.capstone2020.Model.Notification;
import com.skhu.capstone2020.Model.RequestNotification;
import com.skhu.capstone2020.R;

import java.util.List;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {
    private static final int FRIEND_REQUEST_ITEM = 0;
    private static final int SET_DESTINATION_ITEM = 1;
    private List<Notification> notificationsList;
    private Context mContext;

    public NotificationsListAdapter(List<Notification> notificationsList, Context mContext) {
        this.notificationsList = notificationsList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == FRIEND_REQUEST_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_notification_item, parent, false);
            return new ViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (notificationsList.size() != 0) {
            Notification notification = notificationsList.get(position);

            if (notification instanceof RequestNotification) {
                RequestNotification requestNotification = (RequestNotification) notification;
                holder.request_time.setText(requestNotification.getTime());
                holder.request_userName.setText(requestNotification.getUserName());

                if (requestNotification.isSeen()) {
                    holder.notification_seen.setVisibility(View.VISIBLE);
                }

                if (!(requestNotification.getUserImageUrl().equals("default"))) {
                    holder.request_userImage.setPadding(0, 0, 0, 0);
                    Glide.with(mContext)
                            .load(requestNotification.getUserImageUrl())
                            .apply(new RequestOptions().transform(new RoundedCorners(45)))
                            .into(holder.request_userImage);
                }
            } else {
                // 목적지 설정 알림 객체일 때 동작
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (notificationsList.get(position) instanceof RequestNotification) {
            return FRIEND_REQUEST_ITEM;
        } else {
            return SET_DESTINATION_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {                                       // 미완성 (작성 예정)
        LinearLayout notification_seen;
        TextView request_time, request_userName;
        ImageView request_userImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            request_time = itemView.findViewById(R.id.request_time);
            request_userName = itemView.findViewById(R.id.request_user_name);
            request_userImage = itemView.findViewById(R.id.request_user_image);
            notification_seen = itemView.findViewById(R.id.notification_seen);
        }
    }
}
