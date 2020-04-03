package com.skhu.capstone2020;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.skhu.capstone2020.Custom.CustomProgressDialog;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText input_emailToReset;
    Button btn_sendResetEmail;
    ImageView reset_btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        reset_btn_back = findViewById(R.id.reset_btn_back);                                         // 뒤로가기 버튼
        reset_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
                finish();
            }
        });

        btn_sendResetEmail = findViewById(R.id.btn_sendResetEmail);
        btn_sendResetEmail.setOnClickListener(new View.OnClickListener() {                          // 버튼 클릭 시 비밀번호 재설정 이메일 전송
            @Override
            public void onClick(View v) {
                String email = input_emailToReset.getText().toString();
                sendResetEmail(email, v);
            }
        });

        input_emailToReset = findViewById(R.id.input_emailToReset);
        input_emailToReset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {           // 입력값의 길이에 따라 버튼 활성/비활성화
                if (input_emailToReset.getText().length() >= 8) {
                    btn_sendResetEmail.setBackgroundResource(R.drawable.enabled_round_button);
                    btn_sendResetEmail.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.white));
                    btn_sendResetEmail.setEnabled(true);
                } else {
                    btn_sendResetEmail.setBackgroundResource(R.drawable.disabled_round_button);
                    btn_sendResetEmail.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.grey));
                    btn_sendResetEmail.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
        finish();
    }

    public void sendResetEmail(String email, final View view) {                                     // 비밀번호 재설정 이메일 전송
        final CustomProgressDialog dialog = new CustomProgressDialog(ResetPasswordActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.putExtra("isResetPassword", true);
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                } else {
                    dialog.dismiss();
                    Snackbar.make(view, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(ContextCompat.getColor(ResetPasswordActivity.this, R.color.darkBlue))
                            .show();
                }
            }
        });
    }
}
