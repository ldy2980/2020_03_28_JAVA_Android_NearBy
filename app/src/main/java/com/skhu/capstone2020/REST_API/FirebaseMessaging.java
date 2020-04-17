package com.skhu.capstone2020.REST_API;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.Notification.OreoNotification;
import com.skhu.capstone2020.NotificationActivity;

public class FirebaseMessaging extends FirebaseMessagingService {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token();
                        token.setToken(instanceIdResult.getToken());
                        updateToken(token);
                    }
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String receiverId = remoteMessage.getData().get("receiverId");

        if (currentUser != null && receiverId != null && receiverId.equals(currentUser.getUid())) {
            sendRequestNotification(remoteMessage);
        }
    }

    public void sendRequestNotification(RemoteMessage remoteMessage) {
        String userId = remoteMessage.getData().get("userId");
        String userName = remoteMessage.getData().get("userName");
        String title = "알림";
        String body = "새 친구 요청이 있습니다.";
        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getRequestNotificaton(title, body, pendingIntent, defaultSound);
        oreoNotification.getManager().notify(0, builder.build());
    }

    public void updateToken(Token token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(currentUser.getUid())
                    .set(token);
        }
    }
}
