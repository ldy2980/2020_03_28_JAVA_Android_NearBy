package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skhu.capstone2020.Model.Notification;
import com.skhu.capstone2020.Model.RequestNotification;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.R;

import java.util.List;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {
    private static final int FRIEND_REQUEST_ITEM = 0;
    private static final int SET_DESTINATION_ITEM = 1;
    private List<Notification> notificationsList;
    private Context mContext;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

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
                final RequestNotification requestNotification = (RequestNotification) notification;
                holder.request_time.setText(requestNotification.getTime());
                holder.request_userName.setText(requestNotification.getUserName());
                holder.request_user_status_message.setText(requestNotification.getUserStatusMessage());

/*                if (requestNotification.isSeen()) {
                    holder.notification_seen.setVisibility(View.VISIBLE);
                }*/

                if (!(requestNotification.getUserImageUrl().equals("default"))) {
                    holder.request_userImage.setPadding(0, 0, 0, 0);
                    Glide.with(mContext)
                            .load(requestNotification.getUserImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
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

    private void addFriend(final int position) {
        final RequestNotification requestNotification = (RequestNotification) notificationsList.get(position);
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(requestNotification.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(currentUser.getUid())
                                        .collection("Friends")
                                        .document(user.getId())
                                        .set(user);
                            }
                        }
                    }
                });
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(requestNotification.getId())
                                        .collection("Friends")
                                        .document(user.getId())
                                        .set(user);
                            }
                        }
                    }
                });
        removeNotification(position);
    }

    private void removeNotification(final int position) {
        RequestNotification requestNotification = (RequestNotification) notificationsList.get(position);
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .document(requestNotification.getId())
                .delete();
        notificationsList.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {                                       // 미완성 (작성 예정)
        //LinearLayout notification_seen;
        TextView request_time, request_userName, request_user_status_message;
        ImageView request_userImage;
        ImageButton request_approve, request_refuse;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            request_time = itemView.findViewById(R.id.request_time);
            request_userName = itemView.findViewById(R.id.request_user_name);
            request_user_status_message = itemView.findViewById(R.id.request_user_status_message);
            request_userImage = itemView.findViewById(R.id.request_user_image);
            request_approve = itemView.findViewById(R.id.request_approve);
            request_refuse = itemView.findViewById(R.id.request_refuse);
            request_approve.setOnClickListener(listener);
            request_refuse.setOnClickListener(listener);
            //notification_seen = itemView.findViewById(R.id.notification_seen);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.request_approve:
                        addFriend(getAdapterPosition());
                        break;

                    case R.id.request_refuse:
                        removeNotification(getAdapterPosition());
                        break;
                }
            }
        };
    }
}
