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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.skhu.capstone2020.Custom.CustomProgressDialog;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    EditText inputEmail, inputPassword;
    TextView create_account, goToResetPassword;
    ImageView btn_back;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean isResetPassword = getIntent().getBooleanExtra("isResetPassword", false);
        informResetPassword(isResetPassword);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        btn_back = findViewById(R.id.btn_back);

        inputEmail.addTextChangedListener(textWatcher);
        inputPassword.addTextChangedListener(textWatcher);

        btn_back.setOnClickListener(new View.OnClickListener() {                                    // 뒤로가기 버튼 클릭 시
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
                finish();
            }
        });

        goToResetPassword = findViewById(R.id.goToResetPassword);
        goToResetPassword.setOnClickListener(new View.OnClickListener() {                           // 비밀번호 재설정 화면으로 이동
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
                finish();
            }
        });

        create_account = findViewById(R.id.create_account);
        create_account.setOnClickListener(new View.OnClickListener() {                              // 계정 생성 화면으로 이동
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
                finish();
            }
        });

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {                                   // 로그인 버튼 클릭 시
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                login(email, password, v);
            }
        });
    }

    public void informResetPassword(boolean isReset) {                                              // 비밀번호 재설정 시 알림 표시
        if (isReset) {
            View view = findViewById(R.id.login_activity_root);
            Snackbar.make(view, "비밀번호 재설정 메일을 발송했습니다.\n변경 후 다시 로그인 해주세요.", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(LoginActivity.this, R.color.darkBlue))
                    .show();
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {               // 이메일, 비밀번호 입력값에 따라 버튼 활성/비활성화
            if (inputEmail.getText().length() >= 8 && inputPassword.getText().length() >= 6) {
                btn_login.setBackgroundResource(R.drawable.enabled_round_button);
                btn_login.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.white));
                btn_login.setEnabled(true);
            } else {
                btn_login.setBackgroundResource(R.drawable.disabled_round_button);
                btn_login.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.grey));
                btn_login.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
    }

    public void login(String email, String password, final View view) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final CustomProgressDialog dialog = new CustomProgressDialog(LoginActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            dialog.dismiss();
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
                        } else {
                            dialog.dismiss();
                            Snackbar.make(view, "이메일 또는 패스워드를 확인하세요.", BaseTransientBottomBar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(LoginActivity.this, R.color.darkBlue))
                                    .show();
                        }
                    }
                });
    }
}