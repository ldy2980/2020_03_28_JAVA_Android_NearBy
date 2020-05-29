package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.skhu.capstone2020.Adapter.ViewHolder.SelectUserViewHolder;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.UserGroupInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SelectUserActivity extends AppCompatActivity {
    private ImageView select_user_back;
    private TextView select_user_ok;

    private RecyclerView select_user_recycler;
    private SpinKitView select_user_spinKitView;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Member currentMember;
    private FirestoreRecyclerAdapter<Member, SelectUserViewHolder> adapter;

    private List<Member> memberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        Toolbar toolbar = findViewById(R.id.select_user_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        currentMember = documentSnapshot.toObject(Member.class);
                        memberList.add(currentMember);                                              // 현재 유저정보 가져오기
                    }
                });

        select_user_back = findViewById(R.id.select_user_back);
        select_user_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        // 이전 화면으로 이동
                finish();
                overridePendingTransition(0, 0);
            }
        });

        select_user_ok = findViewById(R.id.select_user_ok);
        select_user_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        // 그룹 생성
                createGroup();
            }
        });

        select_user_spinKitView = findViewById(R.id.select_user_spinKitView);

        select_user_recycler = findViewById(R.id.select_user_recycler);
        select_user_recycler.setHasFixedSize(true);
        select_user_recycler.setLayoutManager(new LinearLayoutManager(this));
        loadFriends();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }

    private void loadFriends() {
        Log.d("Test", "loadFriends");
        Query query = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Friends");

        FirestoreRecyclerOptions<Member> options = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Member, SelectUserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SelectUserViewHolder holder, int position, @NonNull Member member) {
                holder.select_user_name.setText(member.getName());
                if (!(member.getImageUrl().equals("default"))) {
                    holder.select_user_photo.setPadding(0, 0, 0, 0);
                    Glide.with(SelectUserActivity.this)
                            .load(member.getImageUrl())
                            .into(holder.select_user_photo);
                }

                holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {               // 그룹에 참여시킬 유저 선택
                    @Override
                    public void onItemClickListener(View view, int position) {
                        if (holder.select_user_check.isClickable()) {
                            holder.select_user_check.setImageResource(R.drawable.ic_check_disabled);
                            holder.select_user_check.setClickable(false);
                            memberList.remove(member);
                            if (memberList.size() <= 1) {
                                select_user_ok.setTextColor(ContextCompat.getColor(SelectUserActivity.this, R.color.ice));
                                select_user_ok.setEnabled(false);
                            }
                            Log.d("Test", "memberList: " + memberList);
                        } else {
                            holder.select_user_check.setImageResource(R.drawable.ic_check_enabled);
                            holder.select_user_check.setClickable(true);
                            memberList.add(member);
                            if (!(select_user_ok.isEnabled())) {
                                select_user_ok.setTextColor(ContextCompat.getColor(SelectUserActivity.this, R.color.black));
                                select_user_ok.setEnabled(true);
                            }
                            Log.d("Test", "memberList: " + memberList);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public SelectUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(SelectUserActivity.this).inflate(R.layout.select_user_item, parent, false);
                return new SelectUserViewHolder(view);
            }
        };
        adapter.startListening();
        select_user_recycler.setAdapter(adapter);
    }

    private void createGroup() {
        Log.d("Test", "createGroup");

        final CustomProgressDialog dialog = new CustomProgressDialog(SelectUserActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();               // CustomProgressDialog 시작

        Collections.shuffle(memberList); //
        CollectionReference groupReference = FirebaseFirestore.getInstance()
                .collection("Groups");
        String groupId = groupReference.document().getId();                                         // 그룹 ID 생성

        int count = memberList.size();          //
        String masterId = currentUser.getUid(); //
        String lastMessage = "";                // 그룹 인원 수, 방장 ID, 마지막 메세지 생성

        Date lastMessageTime = new Date();
/*        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm");
        String lastMessageTime = format.format(date);                               // 마지막 메세지의 시간 정보*/

        StringBuilder groupName = new StringBuilder();                                              // 그룹 이름 생성
        for (int i = 0; i < memberList.size(); ++i) {
            Log.d("Test", "groupName loop");
            if (i == memberList.size() - 1)
                groupName.append(memberList.get(i).getName());
            else
                groupName.append(memberList.get(i).getName()).append(", ");
        }

        Log.d("Test", "escape to groupName loop");

        GroupInfo groupInfo = new GroupInfo(masterId, groupName.toString(), groupId, lastMessage, lastMessageTime, count, memberList);  // 그룹 정보 객체 생성
        UserGroupInfo userGroupInfo = new UserGroupInfo(groupInfo.getGroupId(), masterId);
        Log.d("Test", "success to create groupInfo");

        for (int i = 0; i < memberList.size(); ++i) {                                               // 멤버 유저의 Document에 그룹 ID 저장
            Log.d("Test", "memberList loop");
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(memberList.get(i).getId())
                    .collection("GroupIds")
                    .document(userGroupInfo.getGroupId())
                    .set(userGroupInfo);
        }

        groupReference.document(groupId).set(groupInfo)                                             // 그룹 정보를 DB에 저장
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "success to crete group");
                        Intent intent = new Intent(SelectUserActivity.this, GroupActivity.class);
                        intent.putExtra("groupInfo", groupInfo);
                        startActivity(intent);
                        dialog.dismiss();       // CustomProgressDialog 종료
                        finish();
                        overridePendingTransition(0, 0);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Test", "onFailure");
                        dialog.dismiss();       // ''
                    }
                });
    }
}