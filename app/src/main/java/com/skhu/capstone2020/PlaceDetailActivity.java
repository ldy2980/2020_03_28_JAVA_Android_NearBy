package com.skhu.capstone2020;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Custom.CustomDestinationDialog;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Fragments.DestinationFragment;
import com.skhu.capstone2020.Model.DestinationData;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.GroupSender;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.PlaceResponse.Place;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.Model.UserGroupInfo;
import com.skhu.capstone2020.REST_API.FCMApiService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceDetailActivity extends AppCompatActivity {
    ImageView placeDetail_back;
    TextView placeDetail_name;
    RelativeLayout placeDetail_spinKitView;
    TextView btn_setDestination;

    WebView placeDetail_view;
    WebSettings mWebSettings;

    String url, placeName;

    User loggedUser;
    GroupInfo groupInfo;
    Place place;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    CollectionReference groupsReference = FirebaseFirestore.getInstance().collection("Groups");
    List<GroupInfo> groupInfoList = new ArrayList<>();

    CustomDestinationDialog dialog;
    CustomProgressDialog progressDialog;

    Retrofit retrofit;
    FCMApiService apiService;

    @SuppressLint("StaticFieldLeak")
    public static PlaceDetailActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        activity = PlaceDetailActivity.this;

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {           // 현재 유저정보 가져오기
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null)
                            loggedUser = documentSnapshot.toObject(User.class);
                    }
                });

        groupInfo = (GroupInfo) getIntent().getSerializableExtra("groupInfo");               // 그룹 정보 객체 가져오기
        place = (Place) getIntent().getSerializableExtra("place");                           // 장소 정보 객체 가져오기
        url = getIntent().getStringExtra("url");
        placeName = getIntent().getStringExtra("placeName");

        progressDialog = new CustomProgressDialog(PlaceDetailActivity.this);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(null);
        progressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.placeDetail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        dialog = new CustomDestinationDialog(PlaceDetailActivity.this, okListener, cancelListener);     // 커스텀 다이얼로그 설정
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        placeDetail_spinKitView = findViewById(R.id.placeDetail_spinKitView);
        placeDetail_view = findViewById(R.id.placeDetail_view);

        placeDetail_back = findViewById(R.id.placeDetail_back);
        placeDetail_back.setOnClickListener(new View.OnClickListener() {     // 뒤로 가기
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FirebaseFirestore.getInstance()
                .collection("Groups")
                .whereEqualTo("masterId", currentUser.getUid())     // 현재 유저가 마스터인 그룹이 존재하는지 확인하는 쿼리
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d("Test", "사이즈: " + queryDocumentSnapshots.size());
                        checkGroups(queryDocumentSnapshots);    // 쿼리문 수행 후 얻은 그룹 정보들 확인
                    }
                });

        btn_setDestination = findViewById(R.id.placeDetail_btn_setDestination);            // 목적지 설정 버튼
        btn_setDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "onClick in PlaceDetail");
                if (groupInfo != null) {  // 그룹 화면에서 넘어왔을 경우
                    dialog.show();
                } else {            // 메인화면에서 넘어왔을 경우
                    Intent intent = new Intent(PlaceDetailActivity.this, SelectGroupActivity.class);
                    intent.putExtra("place", place);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });

        placeDetail_name = findViewById(R.id.placeDetail_name);
        placeDetail_name.setText(placeName);

        placeDetail_view.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = placeDetail_view.getSettings(); //세부 세팅
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        placeDetail_view.loadUrl(url);  // 웹뷰 시작
    }

    @Override
    public void onBackPressed() {
        if (placeDetail_view.getOriginalUrl().equalsIgnoreCase(url)) {
            super.onBackPressed();
        } else if (placeDetail_view.canGoBack()) {
            placeDetail_view.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void checkGroups(QuerySnapshot queryDocumentSnapshots) {
        Log.d("Test", "checkGroups");
        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {      // 현재 유저가 마스터인 그룹이 있는 경우
            Log.d("Test", "for문 진입");

            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                UserGroupInfo userGroupInfo = snapshot.toObject(UserGroupInfo.class);
                groupsReference
                        .document(userGroupInfo.getGroupId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {
                                    Log.d("Test", "그룹 정보 가져옴");
                                    GroupInfo groupInfo = documentSnapshot.toObject(GroupInfo.class);
                                    if (groupInfo != null && groupInfo.isSetDestination()) {        // 현재 유저가 마스터인 그룹 중 목적지가 설정된 그룹이 있다면 목적지 설정 버튼 숨기기
                                        Log.d("Test", "목적지 설정됨");
                                        btn_setDestination.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
            }

        } else {
            Log.d("Test", "현재 유저가 마스터인 그룹 없음");
            btn_setDestination.setVisibility(View.GONE);    // 현재 유저가 마스터인 그룹이 하나도 없다면 버튼 숨기기
            placeDetail_spinKitView.setVisibility(View.INVISIBLE);
            placeDetail_view.setVisibility(View.VISIBLE);
        }

        placeDetail_spinKitView.setVisibility(View.INVISIBLE);
        placeDetail_view.setVisibility(View.VISIBLE);
    }

    View.OnClickListener okListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            progressDialog.show();
            SearchPlaceActivity activity = SearchPlaceActivity.activity;
            FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupInfo.getGroupId())
                    .collection("Destination")
                    .document(place.getPlaceId())
                    .set(place)                                            // 목적지로 설정
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendNotification(groupInfo);        // DB에 목적지 정보 저장 후 멤버들에게 푸시 알림 전송
                            dialog.dismiss();
                            Toast.makeText(PlaceDetailActivity.this, "목적지 설정 완료.", Toast.LENGTH_LONG).show();
                            DestinationFragment.isJustSetDestination = true;
                            progressDialog.dismiss();
                            activity.finish();
                            finish();
                        }
                    });

            FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupInfo.getGroupId())
                    .update("setDestination", true);          // 목적지 설정 여부 true로 변경
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };

    private void sendNotification(GroupInfo groupInfo) {
        Log.d("Test", "sendNotification");
        retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(FCMApiService.class);
        List<Member> memberList = groupInfo.getMemberList();

        for (int i = 0; i < memberList.size(); ++i) {
            int finalI = i;
            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(memberList.get(i).getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Token token = documentSnapshot.toObject(Token.class);
                            if (token != null && !memberList.get(finalI).getId().equals(currentUser.getUid())) {
                                Log.d("Test", "member ID: " + memberList.get(finalI).getId());
                                DestinationData destinationData =
                                        new DestinationData(groupInfo.getMasterId(), groupInfo.getGroupId(), groupInfo.getGroupName(), place.getPlaceId(), place.getPlaceName(), memberList.get(finalI).getId());
                                GroupSender groupSender = new GroupSender(destinationData, token.getToken());

                                apiService.sendDestinationNotification(groupSender)
                                        .enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                                                Log.d("Test", "onResponse in sendNotification");
                                                if (!response.isSuccessful()) {
                                                    Log.d("Test", "response: " + response.code() + ", " + response.message());
                                                    return;
                                                }
                                                Log.d("Test", "response: " + response.code() + ", " + response.message());
                                            }

                                            @Override
                                            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                                                Log.d("Test", "onFailure in sendNotification");
                                                Toast.makeText(PlaceDetailActivity.this, "전송 실패", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    });
        }
    }
}
