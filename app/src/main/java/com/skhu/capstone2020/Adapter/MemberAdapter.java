package com.skhu.capstone2020.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.skhu.capstone2020.Model.AddressResponse.Address;
import com.skhu.capstone2020.Model.AddressResponse.AddressResponse;
import com.skhu.capstone2020.Model.GroupInfo;
import com.skhu.capstone2020.Model.Member;
import com.skhu.capstone2020.Model.UserOptions;
import com.skhu.capstone2020.R;
import com.skhu.capstone2020.REST_API.KakaoLocalApi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<Member> memberList;
    private Context context;
    private GroupInfo groupInfo;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private KakaoLocalApi api;
    private Retrofit retrofit;
    private AddressResponse addressResponse;

    private CollectionReference locationReference = FirebaseFirestore.getInstance().collection("Locations");

    public MemberAdapter(List<Member> memberList, GroupInfo groupInfo, Context context) {
        this.memberList = memberList;
        this.groupInfo = groupInfo;
        this.context = context;

        for (int i = 0; i < memberList.size(); ++i)
            if (currentUser.getUid().equals(memberList.get(i).getId())) {
                memberList.remove(i);
                return;
            }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("Test", "onBindViewHolder in MemberAdapter");
        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoLocalApi.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(KakaoLocalApi.class);

        if (memberList.size() != 0) {
            Member member = memberList.get(position);
            holder.member_name.setText(member.getName());

            if (!member.getImageUrl().equals("default")) {
                holder.member_photo.setPadding(0, 0, 0, 0);
                Glide.with(context)
                        .load(member.getImageUrl())
                        .into(holder.member_photo);
            }

            if (member.getId().equals(groupInfo.getMasterId()))                                     // 마스터일 경우 아이콘 표시
                holder.member_master.setVisibility(View.VISIBLE);

            FirebaseFirestore.getInstance()
                    .collection("UserOptions")
                    .document(member.getId())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                UserOptions userOptions = documentSnapshot.toObject(UserOptions.class);
                                if (userOptions != null && !(userOptions.isAllowShareLocation())) {
                                    holder.member_no_tracking.setVisibility(View.VISIBLE);          // 위치 공유 옵션 OFF시 아이콘 표시
                                    holder.member_address.setText("------------");
                                } else {
                                    holder.member_no_tracking.setVisibility(View.INVISIBLE);        //      ''       ON시 아이콘 제거
                                    locationReference.document(member.getId())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot != null) {
                                                        GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                                                        if (geoPoint != null) {
                                                            api.getAddress(KakaoLocalApi.key, geoPoint.getLongitude(), geoPoint.getLatitude())  // 현재 위치를 주소로 변환
                                                                    .enqueue(new Callback<AddressResponse>() {
                                                                        @Override
                                                                        public void onResponse(@NotNull Call<AddressResponse> call, @NotNull Response<AddressResponse> response) {
                                                                            Log.d("Test", "onResponse in MemberAdapter" + ", " + response.message());
                                                                            if (!(response.isSuccessful())) {
                                                                                Toast.makeText(context, response.code() + ", " + response.message(), Toast.LENGTH_LONG).show();
                                                                                return;
                                                                            }
                                                                            addressResponse = response.body();
                                                                            if (addressResponse != null && addressResponse.getAddressDocuments().size() != 0) {
                                                                                Log.d("Test", "addressResponse in MemberAdapter is not null");
                                                                                Address address = addressResponse.getAddressDocuments().get(0).getAddress();
                                                                                if (address != null)
                                                                                    holder.member_address.setText(address.getAddress_name());               // 주소 변환 성공 시
                                                                                else
                                                                                    holder.member_address.setText("현재 위치를 찾을 수 없음");                // 주소 변환 실패 시
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(@NotNull Call<AddressResponse> call, @NotNull Throwable t) {
                                                                            Log.d("Test", "onFailure in MemberAdapter");
                                                                            Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView member_name, member_address;
        ImageView member_photo, member_master, member_no_tracking;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            member_name = itemView.findViewById(R.id.member_name);
            member_address = itemView.findViewById(R.id.member_address);
            member_master = itemView.findViewById(R.id.member_master);
            member_no_tracking = itemView.findViewById(R.id.member_no_tracking);
            member_photo = itemView.findViewById(R.id.member_photo);
            member_photo.setClipToOutline(true);
        }
    }
}
