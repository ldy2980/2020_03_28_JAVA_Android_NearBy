package com.skhu.capstone2020.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.skhu.capstone2020.Fragments.ChatFragment;
import com.skhu.capstone2020.Fragments.DestinationFragment;
import com.skhu.capstone2020.Fragments.MemberFragment;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.User;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private User currentUser;
    private GroupInfo groupInfo;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int pageCount) {
        super(fm, pageCount);
    }

    public ViewPagerAdapter(@NonNull FragmentManager fm, int pageCount, User currentUser, GroupInfo groupInfo) {
        super(fm, pageCount);
        this.currentUser = currentUser;
        this.groupInfo = groupInfo;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DestinationFragment(currentUser, groupInfo);
            case 1:
                return new MemberFragment(groupInfo.getMemberList());
            case 2:
                return new ChatFragment(groupInfo);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
