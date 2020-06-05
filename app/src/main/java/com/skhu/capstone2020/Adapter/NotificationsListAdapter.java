package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.skhu.capstone2020.GroupActivity;
import com.skhu.capstone2020.Model.DestinationNotification;
import com.skhu.capstone2020.Model.Notification;
import com.skhu.capstone2020.Model.RequestNotification;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.R;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {
    private static final int FRIEND_REQUEST_ITEM = 0;
    private static final int SET_DESTINATION_ITEM = 1;
    private List<Notification> notificationsList;
    private Context mContext;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private PrettyTime prettyTime = new PrettyTime();

    public NotificationsListAdapter(List<Notification> notificationsList, Context mContext) {
        this.notificationsList = notificationsList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == FRIEND_REQUEST_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_notification_item, parent, false);
            return new ViewHolder(view, viewType);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.destination_notification_item, parent, false);
            return new ViewHolder(view, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (notificationsList.size() != 0) {
            Notification notification = notificationsList.get(position);

            if (notification instanceof RequestNotification) {
                final RequestNotification requestNotification = (RequestNotification) notification;
                holder.request_time.setText(prettyTime.format(requestNotification.getTime()));
                holder.request_userName.setText(requestNotification.getUserName());
                holder.request_user_status_message.setText(requestNotification.getUserStatusMessage());

                if (!(requestNotification.getUserImageUrl().equals("default"))) {
                    holder.request_userImage.setPadding(0, 0, 0, 0);
                    Glide.with(mContext)
                            .load(requestNotification.getUserImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .apply(new RequestOptions().transform(new RoundedCorners(45)))
                            .into(holder.request_userImage);
                }
            } else if (notification instanceof DestinationNotification) {
                DestinationNotification destinationNotification = (DestinationNotification) notification;
                holder.destination_group_name.setText(destinationNotification.getGroupName());
                holder.set_destination_name.setText(destinationNotification.getPlaceName());
                holder.set_destination_time.setText(prettyTime.format(destinationNotification.getTime()));
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
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notificationsList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView request_time, request_userName, request_user_status_message;
        ImageView request_userImage;
        ImageButton request_approve, request_refuse;

        TextView destination_group_name, set_destination_name, set_destination_time;
        ImageButton set_destination_cancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0) {                                                                        //
                request_time = itemView.findViewById(R.id.request_time);                                //
                request_userName = itemView.findViewById(R.id.request_user_name);                       //
                request_user_status_message = itemView.findViewById(R.id.request_user_status_message);  //
                request_userImage = itemView.findViewById(R.id.request_user_image);                     //
                request_approve = itemView.findViewById(R.id.request_approve);                          //
                request_refuse = itemView.findViewById(R.id.request_refuse);                            //
                request_approve.setOnClickListener(requestListener);                                    //
                request_refuse.setOnClickListener(requestListener);                                     //
            } else if (viewType == 1) {
                itemView.setOnClickListener(this);                                                  //
                destination_group_name = itemView.findViewById(R.id.destination_group_name);        //
                set_destination_name = itemView.findViewById(R.id.set_destination_name);            //
                set_destination_time = itemView.findViewById(R.id.set_destination_time);            //
                set_destination_cancel = itemView.findViewById(R.id.set_destination_cancel);        //
                set_destination_cancel.setOnClickListener(cancelListener);                          // 생성된 뷰홀더 레이아웃에 따라 각각 다른 뷰 할당과 리스너 연결
            }
        }

        View.OnClickListener requestListener = new View.OnClickListener() {
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

        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DestinationNotification destinationNotification = (DestinationNotification) notificationsList.get(getAdapterPosition());
                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(currentUser.getUid())
                        .collection("Notifications")
                        .document(destinationNotification.getGroupId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                notificationsList.remove(getAdapterPosition());
                                //notifyItemRemoved(getAdapterPosition());
                                notifyDataSetChanged();
                            }
                        });
            }
        };

        @Override
        public void onClick(View view) {
            Notification notification = notificationsList.get(getAdapterPosition());
            if (notification instanceof DestinationNotification) {                  // 목적지 설정 알림 항목일 경우
                DestinationNotification destinationNotification = (DestinationNotification) notification;

                destinationNotification.setSeen(true);                          // 해당 항목을 읽음 처리하고 DB에 반영
                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(currentUser.getUid())
                        .collection("Notifications")
                        .document(destinationNotification.getGroupId())
                        .set(destinationNotification);

                Intent intent = new Intent(view.getContext(), GroupActivity.class);         // 목적지 설정 알림 항목 클릭시 그룹 화면으로 이동
                intent.putExtra("groupId", destinationNotification.getGroupId());
                view.getContext().startActivity(intent);
            }
        }
    }
}
