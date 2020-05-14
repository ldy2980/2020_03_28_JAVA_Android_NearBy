package com.skhu.capstone2020.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skhu.capstone2020.R;

public class GroupsFragment extends Fragment {
    private LinearLayout fragment_container;

    public GroupsFragment() {
    }

    public GroupsFragment(LinearLayout container) {
        this.fragment_container = container;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        return view;
    }
}
