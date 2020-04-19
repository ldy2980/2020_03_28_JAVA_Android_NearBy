package com.skhu.capstone2020;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Adapter.NotificationsListAdapter;
import com.skhu.capstone2020.Model.Notification;
import com.skhu.capstone2020.Model.RequestNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    LinearLayout root_view;
    TextView notification_seenAll;
    ImageView notification_back;
    RecyclerView notification_recycler;
    List<Notification> notificationList;
    NotificationsListAdapter adapter;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        root_view = findViewById(R.id.notification_root_view);
        notification_seenAll = findViewById(R.id.notification_seen_all);
        notification_recycler = findViewById(R.id.notification_recycler);
        notification_recycler.setHasFixedSize(true);
        notification_recycler.setLayoutManager(new LinearLayoutManager(this));

        notification_back = findViewById(R.id.notification_back);
        notification_back.setOnClickListener(new View.OnClickListener() {                           // 이전 화면으로 이동
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
            }
        });

        setSeenButton();

        notification_seenAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notification_seenAll.setTextColor(ActivityCompat.getColor(NotificationActivity.this, R.color.grey));
                notification_seenAll.setEnabled(false);
                checkNotificationAll();
            }
        });

        notificationList = new ArrayList<>();
        adapter = new NotificationsListAdapter(notificationList, NotificationActivity.this);
        notification_recycler.setAdapter(adapter);
        loadRequests();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
    }

    public void loadRequests() {                                                                    // 알림 정보 가져오기
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Snackbar.make(root_view, "에러가 발생했습니다.", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(NotificationActivity.this, R.color.darkBlue))
                                    .show();
                            return;
                        }

                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                            notificationList.clear();
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Notification notification = snapshot.toObject(Notification.class);
                                if (notification.getNotificationType() == 0) {
                                    RequestNotification requestNotification = snapshot.toObject(RequestNotification.class);
                                    notificationList.add(requestNotification);
                                } else {
                                    // 목적지 설정 알림 객체일 때 동작할 코드
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void checkNotificationAll() {                                                            // 알림을 모두 읽은 것으로 처리
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() != 0) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Notification notification = snapshot.toObject(Notification.class);
                                if (notification.getNotificationType() == 0) {
                                    RequestNotification requestNotification = snapshot.toObject(RequestNotification.class);
                                    requestNotification.setSeen(true);
                                    FirebaseFirestore.getInstance()
                                            .collection("Users")
                                            .document(currentUser.getUid())
                                            .collection("Notifications")
                                            .document(requestNotification.getId())
                                            .set(requestNotification);
                                } else {
                                    // 목적지 설정 알림객체일 때 동작할 코드
                                }
                            }
                        }
                    }
                });
    }

    public void setSeenButton() {                                                                   // "모두 읽음"버튼 활성/비활성화 여부 확인
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() != 0) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Notification notification = snapshot.toObject(Notification.class);
                                if (!(notification.isSeen())) {
                                    notification_seenAll.setTextColor(ActivityCompat.getColor(NotificationActivity.this, R.color.eastBay));
                                    notification_seenAll.setEnabled(true);
                                    return;
                                }
                            }
                        }
                    }
                });
    }
}
