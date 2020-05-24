package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skhu.capstone2020.Model.Chat;
import com.skhu.capstone2020.R;

import java.util.Date;

public class ChatAdapter extends FirestoreRecyclerAdapter<Chat, ChatAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String groupId;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Chat> options, String groupId, Context context) {
        super(options);
        this.context = context;
        this.groupId = groupId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Chat model) {
        if (holder.getItemViewType() == MSG_TYPE_LEFT)
            holder.chat_userName.setText(model.getUserName());

        holder.chat_item.setText(model.getMessage());
        if (!model.getImageUrl().equals("default")) {
            holder.chat_user_image.setPadding(0, 0, 0, 0);
            Glide.with(context)
                    .load(model.getImageUrl())
                    .into(holder.chat_user_image);
        }

        if (position == getItemCount() - 1)
            sendLastMessageToServer(model.getMessage(), model.getTimeStamp(), groupId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getUserId().equals(currentUser.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView chat_item, chat_userName;
        ImageView chat_user_image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_item = itemView.findViewById(R.id.chat_item);
            chat_userName = itemView.findViewById(R.id.chat_user_name);
            chat_user_image = itemView.findViewById(R.id.chat_user_image);
            chat_user_image.setClipToOutline(true);
        }
    }

    private void sendLastMessageToServer(String lastMessage, Date timeStamp, String groupId) {                      // 마지막 메세지 서버에 저장
        Log.d("Test", "sendLastMessageToServer");
        DocumentReference groupReference = FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupId);

        groupReference.update("lastMessage", lastMessage);
        groupReference.update("lastMessageTime", timeStamp);
    }
}
