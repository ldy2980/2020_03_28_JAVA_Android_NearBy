package com.skhu.capstone2020.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.skhu.capstone2020.Adapter.ChatAdapter;
import com.skhu.capstone2020.Model.Chat;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.User;
import com.skhu.capstone2020.R;

import java.util.Date;
import java.util.Objects;

public class ChatFragment extends Fragment {
    private GroupInfo groupInfo;

    private ImageView btn_chat;
    private EditText chat_input;
    private RecyclerView chat_recycler;
    private ChatAdapter adapter;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private User currentUser;
    private String lastMessage;
    private Date timeStamp;

    public ChatFragment() {
    }

    public ChatFragment(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists())
                            currentUser = documentSnapshot.toObject(User.class);                    // 현재 유저 정보 가져오기
                    }
                });

        chat_input = view.findViewById(R.id.chat_input);
        btn_chat = view.findViewById(R.id.btn_chat);
        chat_recycler = view.findViewById(R.id.chat_recycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);          // 뷰아이템들이 맨 끝에 추가되게 스택 구조 변경
        chat_recycler.setLayoutManager(linearLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupInfo.getGroupId())
                .collection("Chats")
                .orderBy("timeStamp", Query.Direction.ASCENDING);      // 채팅 데이터를 읽어오는 쿼리

        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>().setQuery(query, Chat.class).build();
        adapter = new ChatAdapter(options, groupInfo.getGroupId(), getContext());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                chat_recycler.scrollToPosition(adapter.getItemCount() - 1);                         // 첫 화면에 마지막 메세지가 보이게
            }
        });
        adapter.startListening();
        chat_recycler.setAdapter(adapter);                                                          // 리사이클러뷰 어댑터 설정

        chat_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {           // 입력값이 있을 때만 전송 버튼 활성화
                if (chat_input.getText().toString().trim().length() > 0) {
                    btn_chat.setImageResource(R.drawable.ic_send_eastbay);
                    btn_chat.setClickable(true);
                } else {
                    btn_chat.setImageResource(R.drawable.ic_send_ice);
                    btn_chat.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        chat_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {                // 키보드 엔터키 재정의
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND && chat_input.getText().toString().trim().length() > 0) {
                    InputMethodManager immHide = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (immHide != null)
                        immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    lastMessage = chat_input.getText().toString();      //
                    timeStamp = new Date();                             // 마지막 메세지 정보 저장

                    String message = chat_input.getText().toString();
                    chat_input.setText("");
                    sendChatDataToServer(message);
                }
                return true;
            }
        });

        btn_chat.setOnClickListener(new View.OnClickListener() {                                    // 전송 버튼 클릭 시 동작
            @Override
            public void onClick(View view) {
                if (chat_input.getText().toString().trim().length() > 0) {
                    InputMethodManager immHide = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (immHide != null)
                        immHide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    lastMessage = chat_input.getText().toString();      //
                    timeStamp = new Date();                             // 마지막 메세지 정보 저장

                    String message = chat_input.getText().toString();
                    chat_input.setText("");
                    sendChatDataToServer(message);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        Log.d("Test", "onStart in ChatFragment");
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        Log.d("Test", "onStop in ChatFragment");
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        Log.d("Test", "onDestroyView in ChatFragment");
        super.onDestroyView();
        adapter.stopListening();
    }

    private void sendChatDataToServer(String message) {                                             // 채팅 데이터 서버에 저장
        Log.d("Test", "sendChatDataToServer");
        if (currentUser != null) {
            Chat chat = new Chat(currentUser.getId(), currentUser.getName(), currentUser.getImageUrl(), message, new Date());
            CollectionReference chatReference = FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupInfo.getGroupId())
                    .collection("Chats");
            String chatId = chatReference.document().getId();

            chatReference.document(chatId).set(chat);
        }
    }
}
