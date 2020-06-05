package com.skhu.capstone2020;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skhu.capstone2020.Adapter.ViewHolder.SelectMemberViewHolder;
import com.skhu.capstone2020.Interface.RecyclerItemClickListener;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.User;

import net.daum.mf.map.api.MapView;

import java.util.List;

public class MapViewActivity extends AppCompatActivity {
    private RecyclerView select_member_recycler;
    private TextView select_member_address;

    private GroupInfo groupInfo;
    private User currentUser;

    private RecyclerView.Adapter<SelectMemberViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        groupInfo = (GroupInfo) getIntent().getSerializableExtra("groupInfo");  // 그룹 정보 객체 가져오기
        currentUser = (User) getIntent().getSerializableExtra("currentUser");   // 현재 유저 정보 객체 가져오기

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        ImageView map_view_back = findViewById(R.id.map_view_back);
        map_view_back.setOnClickListener(new View.OnClickListener() {                       // 이전 화면으로 이동
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
            }
        });

        List<Member> memberList = groupInfo.getMemberList();    // 현재 그룹의 멤버 정보
        select_member_address = findViewById(R.id.select_member_address);   // 선택된 멤버의 현재 주소를 표시할 텍스트뷰
        select_member_recycler = findViewById(R.id.select_member_recycler); // 리사이클러뷰
        select_member_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));  // 가로형 리사이클러뷰 설정
        loadMembers(memberList);    // 멤버 목록 로드
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_top);
    }

    private void loadMembers(List<Member> memberList) {     // 멤버 목록 로드
        adapter = new RecyclerView.Adapter<SelectMemberViewHolder>() {  // 어댑터 생성
            @NonNull
            @Override
            public SelectMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MapViewActivity.this).inflate(R.layout.select_member_item, parent, false);
                return new SelectMemberViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull SelectMemberViewHolder holder, int position) {
                if (memberList.size() != 0) {
                    Member member = memberList.get(position);
                    holder.select_member_name.setText(member.getName());

                    if (!member.getImageUrl().equals("default")) {      // 멤버 프로필 이미지 로드
                        holder.select_member_image.setPadding(0, 0, 0, 0);
                        Glide.with(MapViewActivity.this)
                                .load(member.getImageUrl())
                                .into(holder.select_member_image);
                    }

                    holder.setRecyclerItemClickListener(new RecyclerItemClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position) {
                            Log.d("Test", "selectMemberItem clicked");      // 클릭리스너 설정
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return memberList.size();
            }
        };

        select_member_recycler.setAdapter(adapter);     // 리사이클러뷰와 어댑터 연결
    }
}
