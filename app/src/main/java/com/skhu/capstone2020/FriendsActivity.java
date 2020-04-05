package com.skhu.capstone2020;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skhu.capstone2020.Adapter.FriendsListAdapter;
import com.skhu.capstone2020.Model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    ImageView friends_back;

    RecyclerView friends_recycler;
    FriendsListAdapter adapter;
    List<User> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friends_back = findViewById(R.id.friends_back);
        friends_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
            }
        });

        friends_recycler = findViewById(R.id.friends_recycler);
        friends_recycler.setHasFixedSize(true);
        friends_recycler.setLayoutManager(new LinearLayoutManager(this));

        friendsList = new ArrayList<>();
        adapter = new FriendsListAdapter(friendsList, this);
        friends_recycler.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
    }
}
