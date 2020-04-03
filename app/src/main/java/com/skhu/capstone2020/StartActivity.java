package com.skhu.capstone2020;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Model.User;

import java.util.List;
import java.util.Objects;

public class StartActivity extends AppCompatActivity {
    View container;
    Button btn_start;
    TextView signInWithGoogle;

    FirebaseAuth auth = null;
    GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onStart() {
        super.onStart();
        TedPermission.with(this)                                                            // 앱 접근 권한 설정
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("앱 사용을 위해 접근 권한이 필요합니다.\n\n[설정]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  // Google 로그인 설정
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(StartActivity.this, gso);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        container = findViewById(R.id.start_activity_container);

        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {                                   // "시작하기" 버튼 클릭 시, 이메일 로그인 화면으로 이동
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
            }
        });

        signInWithGoogle = findViewById(R.id.googleAccount);
        signInWithGoogle.setOnClickListener(new View.OnClickListener() {                            // 구글 로그인
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                googleLogin(account);                                                    // Google 로그인 실행
            } catch (ApiException e) {
                Toast.makeText(StartActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));    // 이미 로그인되어 있다면, 즉시 메인화면으로 이동
                finish();
            }
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            finish();                                                                               // 권한이 거부될 경우, 앱 종료
        }
    };

    public void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void googleLogin(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        final CustomProgressDialog dialog = new CustomProgressDialog(StartActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                assert document != null;
                                                if (document.exists()) {                            // DB에 이미 해당 계정정보가 저장되어있는지 여부 확인
                                                    dialog.dismiss();
                                                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                                                    finish();
                                                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
                                                } else {
                                                    User user = new User(firebaseUser.getUid(),
                                                            firebaseUser.getDisplayName(),
                                                            firebaseUser.getEmail(),
                                                            Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString());
                                                    setUserInfoToServer(user, firebaseUser.getUid());                   // DB에 해당 계정에 대한 정보가 저장되어있지 않으면 새로 저장

                                                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                                    intent.putExtra("currentUser", user);
                                                    dialog.dismiss();
                                                    startActivity(intent);
                                                    finish();
                                                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_out);
                                                }
                                            }
                                        }
                                    });
                        } else {
                            dialog.dismiss();
                            Snackbar.make(container, "로그인할 수 없습니다.", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(StartActivity.this, R.color.darkBlue))
                                    .show();
                        }
                    }
                });
    }

    public void setUserInfoToServer(User user, String id) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id)
                .set(user);
    }
}
