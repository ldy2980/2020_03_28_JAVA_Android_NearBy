package com.skhu.capstone2020;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Model.Data;
import com.skhu.capstone2020.Model.Sender;
import com.skhu.capstone2020.Model.Token;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.REST_API.FCMApiService;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FriendRequestActivity extends AppCompatActivity {
    LinearLayout root_view;
    ImageView friend_request_back;
    TextView friend_request_ok;
    EditText input_friend_email;

    CustomProgressDialog dialog;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    User currentUser;
    FCMApiService apiService;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .whereEqualTo("id", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            currentUser = snapshot.toObject(User.class);                            // 현재 유저정보 객체 할당
                            return;
                        }
                    }
                });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        root_view = findViewById(R.id.friend_request_root);
        friend_request_back = findViewById(R.id.friend_request_back);
        friend_request_ok = findViewById(R.id.friend_request_ok);
        input_friend_email = findViewById(R.id.input_friend_email);

        friend_request_back.setOnClickListener(new View.OnClickListener() {                         // 이전 화면으로 이동
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        input_friend_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {           // 입력한 값에 따라 버튼 활성/비활성화
                if (input_friend_email.getText().toString().length() >= 6) {
                    friend_request_ok.setTextColor(ContextCompat.getColor(FriendRequestActivity.this, R.color.black));
                    friend_request_ok.setEnabled(true);
                } else {
                    friend_request_ok.setTextColor(ContextCompat.getColor(FriendRequestActivity.this, R.color.grey));
                    friend_request_ok.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        input_friend_email.setOnEditorActionListener(new TextView.OnEditorActionListener() {        // 키보드 엔터키 재정의
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE && input_friend_email.getText().toString().length() >= 6) {
                    InputMethodManager immHide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (immHide != null)
                        immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    String email = input_friend_email.getText().toString();
                    input_friend_email.setText("");
                    checkEmail(email);
                }
                return true;
            }
        });

        friend_request_ok.setOnClickListener(new View.OnClickListener() {                           // 확인 버튼 클릭 시
            @Override
            public void onClick(View view) {
                InputMethodManager immHide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (immHide != null)
                    immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                String email = input_friend_email.getText().toString();
                input_friend_email.setText("");
                checkEmail(email);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);
    }

    public void checkEmail(final String email) {
        Log.d("Test", "checkEmail");
        dialog = new CustomProgressDialog(FriendRequestActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        if (currentUser != null) {
            if (email.equals(currentUser.getEmail())) {
                dialog.dismiss();
                Snackbar.make(root_view, "현재 자신의 이메일입니다.", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(FriendRequestActivity.this, R.color.darkBlue))
                        .show();
                return;
            }
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(currentUser.getId())
                    .collection("Friends")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                String friendEmail = snapshot.toObject(User.class).getEmail();
                                if (email.equals(friendEmail)) {
                                    dialog.dismiss();
                                    Snackbar.make(root_view, "이미 친구로 등록된 유저입니다.", Snackbar.LENGTH_LONG)
                                            .setBackgroundTint(ContextCompat.getColor(FriendRequestActivity.this, R.color.darkBlue))
                                            .show();
                                    return;
                                }
                            }
                            getTargetUser(email);
                        }
                    });
        }
    }

    public void getTargetUser(String email) {                                                       // 해당 이메일을 가진 유저 정보 가져오기
        Log.d("Test", "getTargetUser");
        FirebaseFirestore.getInstance()
                .collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d("Test", "Found a user");
                        if (queryDocumentSnapshots.size() != 0) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                User targetUser = snapshot.toObject(User.class);
                                sendRequest(targetUser);
                            }
                        } else {
                            Log.d("Test", "Can't find user");
                            dialog.dismiss();
                            finish();
                        }
                    }
                });
    }

    public void sendRequest(final User targetUser) {                                                // 요청 전송
        Log.d("Test", "sendRequest");
        retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(FCMApiService.class);
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .document(targetUser.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Token token = documentSnapshot.toObject(Token.class);
                        if (token != null) {
                            Log.d("Test", "Found token");
                            Data data = new Data(currentUser.getId(), currentUser.getName(), currentUser.getImageUrl(), currentUser.getStatusMessage(), targetUser.getId());
                            Sender sender = new Sender(data, token.getToken());

                            apiService.sendRequestNotification(sender)
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            Log.d("Test", "onResponse");
                                            Log.d("Test", "Response: " + response.message() + ", " + response.code());
                                            dialog.dismiss();
                                            Snackbar.make(root_view, "요청 전송 완료.", Snackbar.LENGTH_LONG)
                                                    .setBackgroundTint(ContextCompat.getColor(FriendRequestActivity.this, R.color.darkBlue))
                                                    .show();
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Snackbar.make(root_view, "전송 실패.", Snackbar.LENGTH_LONG)
                                                    .setBackgroundTint(ContextCompat.getColor(FriendRequestActivity.this, R.color.darkBlue))
                                                    .show();
                                        }
                                    });
                        }
                    }
                });
    }
}
