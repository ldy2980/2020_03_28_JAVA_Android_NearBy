package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Model.User;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText register_userName, register_email, register_password;
    Button btn_createAccount;
    TextView goToLogin;
    ImageView register_btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register_userName = findViewById(R.id.register_userName);
        register_email = findViewById(R.id.register_email);
        register_password = findViewById(R.id.register_password);

        register_userName.addTextChangedListener(textWatcher);
        register_email.addTextChangedListener(textWatcher);
        register_password.addTextChangedListener(textWatcher);

        goToLogin = findViewById(R.id.goToLogin);
        goToLogin.setOnClickListener(new View.OnClickListener() {                                   // 로그인 화면으로 이동
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
                finish();
            }
        });

        register_btn_back = findViewById(R.id.register_btn_back);
        register_btn_back.setOnClickListener(new View.OnClickListener() {                           // 이전 화면으로 이동
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
                finish();
            }
        });

        btn_createAccount = findViewById(R.id.btn_createAccount);
        btn_createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = register_userName.getText().toString();
                String email = register_email.getText().toString();
                String password = register_password.getText().toString();
                register(name, email, password, v);
            }
        });
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {               // 유저이름, 이메일, 비밀번호 입력값에 따라 버튼 활성/비활성화
            if (register_userName.getText().length() >= 2 && register_email.getText().length() >= 8 && register_password.getText().length() >= 6) {
                btn_createAccount.setBackgroundResource(R.drawable.slateblue_round_button);
                btn_createAccount.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.white));
                btn_createAccount.setEnabled(true);
            } else {
                btn_createAccount.setBackgroundResource(R.drawable.disabled_round_button);
                btn_createAccount.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.grey));
                btn_createAccount.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void register(final String name, final String email, final String password, final View view) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final CustomProgressDialog dialog = new CustomProgressDialog(RegisterActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            if (auth.getCurrentUser() != null) {
                                User user = new User(firebaseUser.getUid(), name, email, "default");
                                user.setStatusMessage("");
                                setUserInfoToServer(user, firebaseUser.getUid());
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                dialog.dismiss();
                                startActivity(intent);
                            }
                        } else {
                            dialog.dismiss();
                            Snackbar.make(view, "해당 정보로 가입할 수 없습니다.", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(RegisterActivity.this, R.color.darkBlue))
                                    .show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {                                                                   // 단말의 뒤로가기 버튼을 눌렀을 때 호출
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
        finish();
    }

    public void setUserInfoToServer(User user, String id) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id)
                .set(user);
    }
}
