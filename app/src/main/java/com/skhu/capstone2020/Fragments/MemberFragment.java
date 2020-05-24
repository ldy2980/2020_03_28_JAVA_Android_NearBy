package com.skhu.capstone2020.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.R;

import java.util.List;

public class MemberFragment extends Fragment {
    private List<Member> memberList;

    public MemberFragment() {
    }

    public MemberFragment(List<Member> memberList) {
        this.memberList = memberList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);


        return view;
    }
}
