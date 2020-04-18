package com.skhu.capstone2020;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.skhu.capstone2020.Custom.CustomProgressDialog;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.Model.UserOptions;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    FrameLayout profile_image_container;
    TextView btn_logout, profile_userName, profile_userEmail;
    ImageView profile_image, profile_back;
    Switch switch_push, switch_friendRequest, switch_sharedLocation;

    public static int REQUEST_TAKE_ALBUM = 1;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        profile_image_container = findViewById(R.id.profile_image_container);
        profile_image_container.setOnClickListener(new View.OnClickListener() {                     // 프로필 이미지 변경
            @Override
            public void onClick(View v) {
                settingImage();
            }
        });

        profile_image = findViewById(R.id.edit_profile_image);
        profile_back = findViewById(R.id.profile_back);
        profile_back.setOnClickListener(new View.OnClickListener() {                                // 이전 화면으로 이동
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
            }
        });

        profile_userName = findViewById(R.id.profile_userName);
        profile_userEmail = findViewById(R.id.profile_userEmail);
        setUserInfo();                                                                              // 현재 유저정보 설정

        btn_logout = findViewById(R.id.btn_logout);                                                 // 로그아웃 버튼 클릭
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, StartActivity.class));
                ActivityCompat.finishAffinity(ProfileActivity.this);
                //overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
            }
        });

        switch_push = findViewById(R.id.switch_push);
        switch_friendRequest = findViewById(R.id.switch_friendRequest);
        switch_sharedLocation = findViewById(R.id.switch_shareLocation);

        switch_push.setOnCheckedChangeListener(listener);
        switch_friendRequest.setOnCheckedChangeListener(listener);
        switch_sharedLocation.setOnCheckedChangeListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_scale_in, R.anim.anim_slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_ALBUM && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "업로드 진행 중...", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_push:
                    changeOption("allowNotification", isChecked);
                    break;

                case R.id.switch_friendRequest:
                    changeOption("allowFriendRequest", isChecked);
                    break;

                case R.id.switch_shareLocation:
                    changeOption("allowShareLocation", isChecked);
                    break;
            }
        }
    };

    public void changeOption(String option, boolean isChecked) {
        FirebaseFirestore.getInstance()
                .collection("UserOptions")
                .document(firebaseUser.getUid())
                .update(option, isChecked);
    }

    public void setUserInfo() {
        final CustomProgressDialog dialog = new CustomProgressDialog(ProfileActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        assert currentUser != null;
                        profile_userName.setText(currentUser.getName());
                        profile_userEmail.setText(currentUser.getEmail());
                        if (!(currentUser.getImageUrl().equals("default"))) {                       // 유저 프로필이미지 설정
                            profile_image.setPadding(0, 0, 0, 0);
                            Glide.with(ProfileActivity.this)
                                    .load(currentUser.getImageUrl())
                                    .apply(new RequestOptions().transform(new RoundedCorners(52)))
                                    .into(profile_image);
                        }
                    }
                });
        FirebaseFirestore.getInstance()
                .collection("UserOptions")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserOptions userOptions = documentSnapshot.toObject(UserOptions.class);
                        if (userOptions != null) {                                                  // 유저 옵션 설정
                            switch_push.setChecked(userOptions.isAllowNotification());
                            switch_friendRequest.setChecked(userOptions.isAllowFriendRequest());
                            switch_sharedLocation.setChecked(userOptions.isAllowShareLocation());
                        }
                    }
                });
        dialog.dismiss();
    }

    public void settingImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get Album"), REQUEST_TAKE_ALBUM);
    }

    public void uploadImage() {
        final CustomProgressDialog dialog = new CustomProgressDialog(ProfileActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
        dialog.setCancelable(false);
        dialog.show();
        storageReference = FirebaseStorage.getInstance().getReference("profileImages");
        if (imageUri != null) {
/*            final StorageReference fileReference = storageReference.child(firebaseUser.getUid()).child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));*/
            final StorageReference fileReference = storageReference.child(firebaseUser.getUid()).child(firebaseUser.getUid()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(firebaseUser.getUid())
                                .update("imageUrl", mUri)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        setUserInfo();
                                    }
                                });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "이미지가 선택되지 않음", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}