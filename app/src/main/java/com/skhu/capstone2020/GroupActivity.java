package com.skhu.capstone2020;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skhu.capstone2020.Adapter.ViewPagerAdapter;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.User;

import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {
    private ViewPager group_frag_container;
    private ViewPagerAdapter adapter;
    private BottomNavigationView group_bottom_navigation;
    @SuppressLint("StaticFieldLeak")
    public static TextView group_toolbar_title;

    private GroupInfo groupInfo;

    private User currentUser;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupInfo = (GroupInfo) getIntent().getSerializableExtra("groupInfo");     // 그룹 정보 객체 가져오기
        String groupId = getIntent().getStringExtra("groupId");
        if (groupInfo == null && groupId != null) {                         // 그룹 정보 객체가 null이면 DB에서 가져와 할당
            FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null)
                                groupInfo = documentSnapshot.toObject(GroupInfo.class);

                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists())
                                                currentUser = documentSnapshot.toObject(User.class);                    // 현재 유저 정보 가져오기

                                            updateMemberInfo(currentUser, groupInfo);                                   // 멤버 정보 업데이트
                                            adapter = new ViewPagerAdapter(getSupportFragmentManager(), group_bottom_navigation.getMaxItemCount(), currentUser, groupInfo);
                                            group_frag_container.setAdapter(adapter);

                                            group_toolbar_title = findViewById(R.id.group_toolbar_title);
                                            group_toolbar_title.setText(groupInfo.getGroupName());      // 그룹 이름 표시
                                        }
                                    });
                        }
                    });
        } else {                                                     // 그룹 정보 객체가 null이 아닌 경우
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists())
                                currentUser = documentSnapshot.toObject(User.class);                    // 현재 유저 정보 가져오기
                            updateMemberInfo(currentUser, groupInfo);                                   // 멤버 정보 업데이트
                            adapter = new ViewPagerAdapter(getSupportFragmentManager(), group_bottom_navigation.getMaxItemCount(), currentUser, groupInfo);
                            group_frag_container.setAdapter(adapter);
                        }
                    });
            group_toolbar_title = findViewById(R.id.group_toolbar_title);
            group_toolbar_title.setText(groupInfo.getGroupName());       // 그룹 이름 표시
        }

        Toolbar toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        group_frag_container = findViewById(R.id.group_frag_container);
        group_bottom_navigation = findViewById(R.id.group_bottom_navigation);

        group_bottom_navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {                                                             // BottomNavigationView 설정
                case R.id.nav_destination:
                    group_frag_container.setCurrentItem(0);
                    break;
                case R.id.nav_members:
                    group_frag_container.setCurrentItem(1);
                    break;
                case R.id.nav_chat:
                    group_frag_container.setCurrentItem(2);
                    break;
            }
            return false;
        });

        //adapter = new ViewPagerAdapter(getSupportFragmentManager(), group_bottom_navigation.getMaxItemCount(), currentUser, groupInfo);
        //group_frag_container.setAdapter(adapter);
        group_frag_container.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {         // ViewPager 설정
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                group_bottom_navigation.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
    }

    private void updateMemberInfo(User currentUser, GroupInfo groupInfo) {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("Groups").document(groupInfo.getGroupId());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int index = 0;
                    GroupInfo info = documentSnapshot.toObject(GroupInfo.class);
                    if (info != null) {
                        List<Member> memberList = info.getMemberList();
                        for (int i = 0; i < memberList.size(); ++i) {
                            if (currentUser.getId().equals(memberList.get(i).getId())) {
                                index = i;
                            }
                        }
                        Member member = new Member(currentUser.getId(), currentUser.getName(), currentUser.getImageUrl());
                        memberList.remove(index);
                        memberList.add(member);
                        reference.update("memberList", memberList);
                    }
                }
            }
        });
    }
}
