package com.skhu.capstone2020;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.skhu.capstone2020.Model.DestinationNotification;
import com.skhu.capstone2020.Model.RequestNotification;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.Notification.OreoNotification;

import java.util.Date;

public class FirebaseMessaging extends FirebaseMessagingService {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("Test", "onNewToken");
        updateToken(new Token(s));
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
        Log.d("Test", "onMessageReceived");
        String receiverId = remoteMessage.getData().get("receiverId");
        Log.d("Test", "getData(): " + receiverId);

        String memberId = remoteMessage.getData().get("memberId");

        if (currentUser != null && receiverId != null && receiverId.equals(currentUser.getUid())) {     // 친구 요청일 경우
            sendRequestNotification(remoteMessage);
        } else if (currentUser != null && memberId != null && memberId.equals(currentUser.getUid())) {  // 목적지 설정 알림일 경우
            sendDestinationNotification(remoteMessage);
        }
    }

    public void sendRequestNotification(RemoteMessage remoteMessage) {
        Log.d("Test", "sendRequestNotification");
        String userId = remoteMessage.getData().get("userId");
        String userName = remoteMessage.getData().get("userName");
        String userImage = remoteMessage.getData().get("userImage");
        String userStatusMessage = remoteMessage.getData().get("userStatusMessage");
        String title = "알림";
        String body = "새 친구 요청이 있습니다.";

        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getRequestNotification(title, body, pendingIntent, defaultSound);
        oreoNotification.getManager().notify(0, builder.build());

        Date requestTime = new Date();
        RequestNotification requestNotification = new RequestNotification(userId, userName, userImage, userStatusMessage, requestTime);
        addRequestNotification(requestNotification);
    }

    public void addRequestNotification(RequestNotification requestNotification) {
        Log.d("Test", "addRequestNotification");
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .document(requestNotification.getId())
                .set(requestNotification);
    }

    public void sendDestinationNotification(RemoteMessage remoteMessage) {
        Log.d("Test", "sendDestinationNotification");
        String masterId = remoteMessage.getData().get("masterId");
        String groupId = remoteMessage.getData().get("groupId");
        String groupName = remoteMessage.getData().get("groupName");
        String placeId = remoteMessage.getData().get("placeId");
        String placeName = remoteMessage.getData().get("placeName");
        String body = "목적지가 설정되었습니다.";

        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getRequestNotification(groupName, body, pendingIntent, defaultSound);
        oreoNotification.getManager().notify(0, builder.build());

        DestinationNotification destinationNotification = new DestinationNotification(groupId, groupName, placeId, placeName, masterId, new Date());
        addDestinationNotification(destinationNotification);
    }

    public void addDestinationNotification(DestinationNotification destinationNotification) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .document(destinationNotification.getId())
                .set(destinationNotification);
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
