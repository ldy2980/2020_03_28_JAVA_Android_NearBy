package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Adapter.FriendsListAdapter;
import com.skhu.capstone2020.Model.User;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    ImageView friends_back, btn_addFriend;

    SpinKitView friends_spinKitView;
    RecyclerView friends_recycler;
    FriendsListAdapter adapter;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friends_spinKitView = findViewById(R.id.friends_spinKitView);

        friends_back = findViewById(R.id.friends_back);
        friends_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
            }
        });

        btn_addFriend = findViewById(R.id.btn_addFriend);
        btn_addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FriendsActivity.this, FriendRequestActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        friends_recycler = findViewById(R.id.friends_recycler);
        friends_recycler.setHasFixedSize(true);
        friends_recycler.setLayoutManager(new LinearLayoutManager(this));
        loadFriends();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
    }

    public void loadFriends() {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .collection("Friends")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> list = queryDocumentSnapshots.toObjects(User.class);
                        adapter = new FriendsListAdapter(list, FriendsActivity.this);
                        friends_recycler.setAdapter(adapter);
                        friends_recycler.setVisibility(View.VISIBLE);
                        friends_spinKitView.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
