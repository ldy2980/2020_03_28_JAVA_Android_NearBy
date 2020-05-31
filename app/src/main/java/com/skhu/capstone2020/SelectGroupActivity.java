package com.skhu.capstone2020;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.skhu.capstone2020.Adapter.ViewHolder.SelectGroupViewHolder;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Custom.CustomSelectGroupDialog;
import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.Model.DestinationData;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.GroupSender;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.REST_API.FCMApiService;

import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectGroupActivity extends AppCompatActivity {
    private FirestoreRecyclerAdapter<GroupInfo, SelectGroupViewHolder> adapter;
    private RecyclerView select_group_recycler;
    private CustomSelectGroupDialog dialog;
    private CustomProgressDialog progressDialog;
    private Place place;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private PrettyTime time = new PrettyTime();
    private Retrofit retrofit;
    private FCMApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        place = (Place) getIntent().getSerializableExtra("place");

        progressDialog = new CustomProgressDialog(SelectGroupActivity.this);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(null);
        progressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.select_group_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        ImageView select_group_back = findViewById(R.id.select_group_back);
        select_group_back.setOnClickListener(new View.OnClickListener() {           // 툴바의 뒤로가기 버튼 클릭 시
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        select_group_recycler = findViewById(R.id.select_group_recycler);
        select_group_recycler.setHasFixedSize(true);
        select_group_recycler.setLayoutManager(new LinearLayoutManager(this));
        loadGroups();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);
    }

    private void loadGroups() {         // 그룹 목록 가져오기
        Query query = FirebaseFirestore.getInstance()
                .collection("Groups")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<GroupInfo> options = new FirestoreRecyclerOptions.Builder<GroupInfo>()
                .setQuery(query, GroupInfo.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<GroupInfo, SelectGroupViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SelectGroupViewHolder holder, int position, @NonNull GroupInfo groupInfo) {
                if (groupInfo.getMasterId().equals(currentUser.getUid())) {
                    Log.d("Test", "Current user is master");
                    holder.group_name.setText(groupInfo.getGroupName());
                    holder.group_count.setTextColor(groupInfo.getCount());
                    holder.member_last_message.setText(groupInfo.getLastMessage());
                    holder.member_last_message_time.setText(time.format(groupInfo.getLastMessageTime()));

                    List<Member> memberList = groupInfo.getMemberList();
                    List<ImageView> imageViewList = holder.getImageViewList();
                    for (int i = 0; i < memberList.size(); ++i) {
                        if (i > 3)
                            return;
                        if (!(memberList.get(i).getImageUrl().equals("default"))) {
                            imageViewList.get(i).setPadding(0, 0, 0, 0);
                            Glide.with(SelectGroupActivity.this)
                                    .load(memberList.get(i).getImageUrl())
                                    .into(imageViewList.get(i));
                        }
                    }

                    holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position) {                  // 목적지를 설정하려는 그룹을 클릭 시
                            setDestination(groupInfo, place);
                        }
                    });
                }
            }

            @NonNull
            @Override
            public SelectGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(SelectGroupActivity.this).inflate(R.layout.group_item, parent, false);
                return new SelectGroupViewHolder(view);
            }
        };
        adapter.startListening();
        select_group_recycler.setAdapter(adapter);
    }

    private void setDestination(GroupInfo groupInfo, Place place) {              // 선택한 그룹의 목적지로 설정
        dialog = new CustomSelectGroupDialog(SelectGroupActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                FirebaseFirestore.getInstance()
                        .collection("Groups")
                        .document(groupInfo.getGroupId())
                        .collection("Destination")
                        .document(place.getPlaceId())
                        .set(place)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendNotification(groupInfo);            // DB에 목적지 정보 저장 후 멤버들에게 푸시 알림 전송
                                PlaceDetailActivity activity = PlaceDetailActivity.activity;
                                Intent intent = new Intent(SelectGroupActivity.this, GroupActivity.class);  //
                                intent.putExtra("groupInfo", groupInfo);
                                dialog.dismiss();
                                Toast.makeText(SelectGroupActivity.this, "목적지 설정 완료.", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                startActivity(intent);      // 그룹 액티비티로 이동
                                activity.finish();
                                finish();
                                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_scale_out);
                            }
                        });
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });     // 커스텀 다이얼로그 설정
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    private void sendNotification(GroupInfo groupInfo) {
        Log.d("Test", "sendNotification");
        retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(FCMApiService.class);
        List<Member> memberList = groupInfo.getMemberList();

        for (int i = 0; i < memberList.size(); ++i) {
            int finalI = i;
            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(memberList.get(i).getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Token token = documentSnapshot.toObject(Token.class);
                            if (token != null && !memberList.get(finalI).getId().equals(currentUser.getUid())) {
                                Log.d("Test", "member ID: " + memberList.get(finalI).getId());
                                DestinationData destinationData =
                                        new DestinationData(groupInfo.getMasterId(), groupInfo.getGroupId(), groupInfo.getGroupName(), place.getPlaceId(), place.getPlaceName(), memberList.get(finalI).getId());
                                GroupSender groupSender = new GroupSender(destinationData, token.getToken());

                                apiService.sendDestinationNotification(groupSender)
                                        .enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                                                Log.d("Test", "onResponse in sendNotification of SelectGroupActivity");
                                                if (!response.isSuccessful()) {
                                                    Log.d("Test", "response: " + response.code() + ", " + response.message());
                                                    return;
                                                }
                                                Log.d("Test", "response: " + response.code() + ", " + response.message());
                                            }

                                            @Override
                                            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                                                Log.d("Test", "onFailure in sendNotification of SelectGroupActivity");
                                                Toast.makeText(SelectGroupActivity.this, "전송 실패", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    });
        }
    }
}
