package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.skhu.capstone2020.Fragments.GroupsFragment;
import com.skhu.capstone2020.Fragments.MyLocationFragment;
import com.skhu.capstone2020.Fragments.SurroundingFragment;
import com.skhu.capstone2020.Model.Notification;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.Model.User;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    LinearLayout drawer_logout, drawer_friends, drawer_setting, drawer_notification;
    RelativeLayout fragment_container;
    View drawerView;
    ImageView btn_surrounding, btn_myLocation, btn_chats, drawerMenu, new_sign, drawer_new_sign, btn_search;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();                          // 현재 유저정보 객체 생성

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment_container = findViewById(R.id.fragment_container);

        updateToken();
        updateFriendsInfo();

        new_sign = findViewById(R.id.new_sign);
        drawer_new_sign = findViewById(R.id.drawer_new_sign);
        checkNotifications();

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer_view);
        drawerMenu = findViewById(R.id.drawer_menu);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawerMenu.setOnClickListener(new View.OnClickListener() {                                  // 네비게이션 드로어 호출
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        btn_search = findViewById(R.id.btn_search);                                                 // 장소 검색 화면으로 이동
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this, MapViewActivity.class));
                startActivity(new Intent(MainActivity.this, SearchPlaceActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_scale_out);
            }
        });

        drawer_friends = findViewById(R.id.drawer_friends);
        drawer_friends.setOnClickListener(new View.OnClickListener() {                              // 친구 목록 화면으로 이동
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FriendsActivity.class));
                drawerLayout.closeDrawer(drawerView);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
            }
        });

        drawer_notification = findViewById(R.id.drawer_notification);
        drawer_notification.setOnClickListener(new View.OnClickListener() {                         // 알림 화면으로 이동
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                drawerLayout.closeDrawer(drawerView);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
            }
        });

        drawer_setting = findViewById(R.id.drawer_setting);
        drawer_setting.setOnClickListener(new View.OnClickListener() {                              // 설정 화면으로 이동
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                drawerLayout.closeDrawer(drawerView);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
            }
        });

        drawer_logout = findViewById(R.id.drawer_logout);
        drawer_logout.setOnClickListener(new View.OnClickListener() {                               // 로그아웃 버튼 클릭 시
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                ActivityCompat.finishAffinity(MainActivity.this);
            }
        });

        btn_surrounding = findViewById(R.id.btn_surrounding);
        btn_myLocation = findViewById(R.id.btn_myLocation);
        btn_chats = findViewById(R.id.btn_groups);

        btn_surrounding.setFocusableInTouchMode(true);
        btn_myLocation.setFocusableInTouchMode(true);
        btn_chats.setFocusableInTouchMode(true);

        btn_surrounding.requestFocus();

        btn_surrounding.setOnClickListener(onClickListener);
        btn_myLocation.setOnClickListener(onClickListener);
        btn_chats.setOnClickListener(onClickListener);

        btn_surrounding.setOnFocusChangeListener(focusChangeListener);
        btn_myLocation.setOnFocusChangeListener(focusChangeListener);
        btn_chats.setOnFocusChangeListener(focusChangeListener);

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new SurroundingFragment()).commitAllowingStateLoss();  // 첫번째로 보일 프래그먼트 띄우기
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerView)) {                                                // 단말의 뒤로가기 버튼을 누를 때 드로어가 열려있다면, 드로어를 닫음
            drawerLayout.closeDrawer(drawerView);
        } else {
            super.onBackPressed();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.requestFocus();
        }
    };

    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {             // Bottom Icon 클릭 시 동작
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            transaction = fragmentManager.beginTransaction();
            switch (view.getId()) {
                case R.id.btn_surrounding:
                    if (hasFocus) {
/*                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fragment_container.getLayoutParams();
                        params.topMargin = 100;
                        params.bottomMargin = 80;
                        fragment_container.setLayoutParams(params);*/
                        transaction.replace(R.id.fragment_container, new SurroundingFragment()).commitAllowingStateLoss();
                        btn_surrounding.setImageResource(R.drawable.ic_surrounding_black);
                    } else {
                        btn_surrounding.setImageResource(R.drawable.ic_surrounding_white);
                    }
                    break;

                case R.id.btn_myLocation:
                    if (hasFocus) {
/*                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fragment_container.getLayoutParams();
                        params.topMargin = 0;
                        params.bottomMargin = 0;
                        fragment_container.setLayoutParams(params);*/
                        transaction.replace(R.id.fragment_container, new MyLocationFragment()).commitAllowingStateLoss();
                        btn_myLocation.setImageResource(R.drawable.ic_location_black);
                    } else {
                        btn_myLocation.setImageResource(R.drawable.ic_location_white);
                    }
                    break;

                case R.id.btn_groups:
                    if (hasFocus) {
                        transaction.replace(R.id.fragment_container, new GroupsFragment()).commitAllowingStateLoss();
                        btn_chats.setImageResource(R.drawable.ic_chat_black);
                    } else {
                        btn_chats.setImageResource(R.drawable.ic_chat_white);
                    }
                    break;
            }
        }
    };

    public void updateFriendsInfo() {
        final CollectionReference friendColRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Friends");
        final CollectionReference userColRef = FirebaseFirestore.getInstance().collection("Users");

        friendColRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            String userId = snapshot.toObject(User.class).getId();
                            userColRef.document(userId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            User user = documentSnapshot.toObject(User.class);
                                            if (user != null)
                                                friendColRef.document(user.getId()).set(user);
                                        }
                                    });
                        }
                    }
                });
    }

    public void checkNotifications() {                                                              // 미확인된 알림이 있을 때 메인화면에 표시
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Notification notification = snapshot.toObject(Notification.class);
                                if (!(notification.isSeen())) {
                                    new_sign.setVisibility(View.VISIBLE);
                                    drawer_new_sign.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                            new_sign.setVisibility(View.INVISIBLE);
                            drawer_new_sign.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public void updateToken() {                                                                     // 토큰 값 DB에 저장
        Log.d("Test", "updateToken");
        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        FirebaseFirestore
                                .getInstance()
                                .collection("Tokens")
                                .document(currentUser.getUid())
                                .set(token);
                    }
                });
    }
}