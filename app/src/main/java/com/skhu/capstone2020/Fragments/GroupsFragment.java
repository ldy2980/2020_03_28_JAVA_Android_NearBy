package com.skhu.capstone2020.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Adapter.GroupListAdapter;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.SelectUserActivity;

import java.util.List;
import java.util.Objects;

public class GroupsFragment extends Fragment {
    private LinearLayout fragment_container;
    private RecyclerView group_recycler;
    private GroupListAdapter adapter;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private ImageView btn_addGroup;

    public GroupsFragment() {
    }

    public GroupsFragment(LinearLayout container) {
        this.fragment_container = container;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        btn_addGroup = view.findViewById(R.id.btn_addGroup);
        btn_addGroup.setOnClickListener(new View.OnClickListener() {                                // 그룹 생성을 위해 유저 선택 화면으로 이동
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SelectUserActivity.class));
                Objects.requireNonNull(getActivity()).overridePendingTransition(0, 0);
            }
        });

        group_recycler = view.findViewById(R.id.group_recycler);
        group_recycler.setHasFixedSize(true);
        group_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        loadGroups();

        return view;
    }

    private void loadGroups() {
        Log.d("Test", "loadGroups");
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Groups")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!(queryDocumentSnapshots.isEmpty())) {
                            List<GroupInfo> groupInfoList = queryDocumentSnapshots.toObjects(GroupInfo.class);
                            adapter = new GroupListAdapter(groupInfoList, getContext());
                            group_recycler.setAdapter(adapter);
                        }
                    }
                });
    }
}
