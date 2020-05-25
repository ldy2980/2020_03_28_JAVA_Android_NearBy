package com.skhu.capstone2020.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.MemberAdapter;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.R;

import java.util.List;
import java.util.Objects;

public class MemberFragment extends Fragment {
    private List<Member> memberList;
    private GroupInfo groupInfo;

    public MemberFragment() {
    }

    public MemberFragment(List<Member> memberList, GroupInfo groupInfo) {
        this.memberList = memberList;
        this.groupInfo = groupInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);

        RecyclerView member_recycler = view.findViewById(R.id.member_recycler);
        member_recycler.setHasFixedSize(true);
        member_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        member_recycler.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        MemberAdapter adapter = new MemberAdapter(memberList, groupInfo, getContext());
        member_recycler.setAdapter(adapter);                                                        // 리사이클러뷰 설정

        return view;
    }
}
