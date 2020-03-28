package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.skhu.capstone2020.Fragments.ChatsFragment;
import com.skhu.capstone2020.Fragments.MyLocationFragment;
import com.skhu.capstone2020.Fragments.SurroundingFragment;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    LinearLayout drawer_logout;
    View drawerView;
    RelativeLayout fragment_container;
    ImageView btn_surrounding, btn_myLocation, btn_chats, drawerMenu;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment_container = findViewById(R.id.fragment_container);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer_view);
        drawerMenu = findViewById(R.id.drawer_menu);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawerMenu.setOnClickListener(new View.OnClickListener() {                                  // 네비게이션 드로어 호출
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        drawer_logout = findViewById(R.id.drawer_logout);
        drawer_logout.setOnClickListener(new View.OnClickListener() {                               // 로그아웃 버튼 클릭 시 동작
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
            }
        });

        btn_surrounding = findViewById(R.id.btn_surrounding);
        btn_myLocation = findViewById(R.id.btn_myLocation);
        btn_chats = findViewById(R.id.btn_chats);

        btn_surrounding.setFocusableInTouchMode(true);
        btn_myLocation.setFocusableInTouchMode(true);
        btn_chats.setFocusableInTouchMode(true);

        btn_surrounding.requestFocus();

        btn_surrounding.setOnClickListener(onClickListener);
        btn_myLocation.setOnClickListener(onClickListener);
        btn_chats.setOnClickListener(onClickListener);

        btn_surrounding.setOnFocusChangeListener(focusChangeListener);
        btn_myLocation.setOnFocusChangeListener(focusChangeListener);
        btn_chats.setOnFocusChangeListener(focusChangeListener);

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new SurroundingFragment()).commitAllowingStateLoss();  // 첫번째로 보일 프래그먼트 띄우기
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerView)) {                                                // 단말의 뒤로가기 버튼을 누를 때 드로어가 열려있다면, 드로어를 닫음
            drawerLayout.closeDrawer(drawerView);
        } else {
            super.onBackPressed();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.requestFocus();
        }
    };

    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {             // Bottom Icon 클릭 시 동작
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            transaction = fragmentManager.beginTransaction();
            switch (view.getId()) {
                case R.id.btn_surrounding:
                    if (hasFocus) {
                        transaction.replace(R.id.fragment_container, new SurroundingFragment()).commitAllowingStateLoss();
                        btn_surrounding.setImageResource(R.drawable.ic_surrounding_black);
                    } else {
                        btn_surrounding.setImageResource(R.drawable.ic_surrounding_white);
                    }
                    break;

                case R.id.btn_myLocation:
                    if (hasFocus) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fragment_container.getLayoutParams();
                        params.topMargin = 0;
                        fragment_container.setLayoutParams(params);
                        transaction.replace(R.id.fragment_container, new MyLocationFragment()).commitAllowingStateLoss();
                        btn_myLocation.setImageResource(R.drawable.ic_location_black);
                    } else {
                        btn_myLocation.setImageResource(R.drawable.ic_location_white);
                    }
                    break;

                case R.id.btn_chats:
                    if (hasFocus) {
                        transaction.replace(R.id.fragment_container, new ChatsFragment()).commitAllowingStateLoss();
                        btn_chats.setImageResource(R.drawable.ic_chat_black);
                    } else {
                        btn_chats.setImageResource(R.drawable.ic_chat_white);
                    }
                    break;
            }
        }
    };
}
