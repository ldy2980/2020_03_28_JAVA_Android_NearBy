package com.skhu.capstone2020.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.SearchPlaceActivity;

import java.util.Objects;

public class DestinationFragment extends Fragment {
    private User currentUser;
    private GroupInfo groupInfo;

    private RelativeLayout layout_no_destination, layout_set_destination;

    private SpinKitView destination_spinKitView;
    private Button btn_search_destination;

    public DestinationFragment(User currentUser, GroupInfo groupInfo) {
        this.currentUser = currentUser;
        this.groupInfo = groupInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destination, container, false);

        destination_spinKitView = view.findViewById(R.id.destination_spinKitView);

        btn_search_destination = view.findViewById(R.id.btn_search_destination);
        btn_search_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchPlaceActivity.class);                // 목적지 설정 화면으로 이동
                intent.putExtra("groupInfo", groupInfo);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_scale_out);
            }
        });

        layout_set_destination = view.findViewById(R.id.set_destination);
        layout_no_destination = view.findViewById(R.id.no_destination);
        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupInfo.getGroupId())
                .collection("Destination")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {                                     // 목적지가 설정되어있지 않을 경우
                            destination_spinKitView.setVisibility(View.INVISIBLE);
                            layout_no_destination.setVisibility(View.VISIBLE);
                            if (currentUser.getId().equals(groupInfo.getMasterId()))
                                btn_search_destination.setVisibility(View.VISIBLE);         // 마스터일 경우 장소 검색 버튼 보이기
                            else
                                btn_search_destination.setVisibility(View.GONE);            // 마스터가 아닐 경우 버튼 숨기기
                        } else {
                            destination_spinKitView.setVisibility(View.INVISIBLE);                  // 목적지가 설정되어있는 경우
                            layout_no_destination.setVisibility(View.INVISIBLE);
                            layout_set_destination.setVisibility(View.VISIBLE);
                        }
                    }
                });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        layout_no_destination.setVisibility(View.INVISIBLE);
        destination_spinKitView.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupInfo.getGroupId())
                .collection("Destination")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {                                     // 목적지가 설정되어있지 않을 경우
                            destination_spinKitView.setVisibility(View.INVISIBLE);
                            layout_no_destination.setVisibility(View.VISIBLE);
                            if (currentUser.getId().equals(groupInfo.getMasterId()))
                                btn_search_destination.setVisibility(View.VISIBLE);         // 마스터일 경우 장소 검색 버튼 보이기
                            else
                                btn_search_destination.setVisibility(View.GONE);            // 마스터가 아닐 경우 버튼 숨기기
                        } else {
                            destination_spinKitView.setVisibility(View.INVISIBLE);                  // 목적지가 설정되어있는 경우
                            layout_no_destination.setVisibility(View.INVISIBLE);
                            layout_set_destination.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
